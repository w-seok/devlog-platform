package com.devlog.platform.common.config;

import static com.devlog.platform.common.util.RequestUtils.*;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IpAccessManager {

	private final ListRanges privatelistRanges;
	private final ListRanges metricsRanges;
	private final ListRanges instanceRanges;

	/**
	 * @param privateNetworks 사설 네트워크 IP 목록
	 * @param metricsNetworks 메트릭 네트워크 IP 목록
	 * @param privateInstances 사설 인스턴스 IP 목록
	 * @throws UnknownHostException IP 주소 파싱 중 오류 발생 시
	 */
	public IpAccessManager(
		@Value("#{'${whitelist.private:}'.replaceAll('\\s+', '').split(',')}") Set<String> privateNetworks,
		@Value("#{'${whitelist.metrics:}'.replaceAll('\\s+', '').split(',')}") Set<String> metricsNetworks,
		@Value("#{'${whitelist.private-instance:}'.replaceAll('\\s+', '').split(',')}") Set<String> privateInstances)
		throws UnknownHostException {

		log.info("\nprivate-networks: {}\n", privateNetworks);
		log.info("\nmetrics: {}\n", metricsNetworks);
		log.info("\nprivate-instance: {}\n", privateInstances);

		this.privatelistRanges = new ListRanges(privateNetworks);
		this.metricsRanges = new ListRanges(metricsNetworks);
		this.instanceRanges = new ListRanges(privateInstances);

		log.info("IpAccessManager 초기화. whitelist: {}, metrics: {}", privatelistRanges, metricsRanges);
	}

	public boolean contains(HttpServletRequest request) {
		String ipStr = getIp(request);

		if (ipStr == null) {
			return false;
		}

		try {
			return privatelistRanges.isAllowed(ipStr);
		} catch (UnknownHostException e) {
			log.error("ip 체크하다 오류 {}", ipStr, e);
			return false;
		}
	}

	public boolean isPrivateNetwork(Authentication authentication, HttpServletRequest request) {
		boolean access = contains(request);

		if (!access) {
			log.info("제한구역 접근[{}]: [{}] [{}]", getIp(request), request.getRequestURI(), authentication);
		}

		return access;
	}

	public boolean isMetricNetwork(HttpServletRequest request) {
		try {
			return metricsRanges.isAllowed(getIp(request));
		} catch (UnknownHostException e) {
			log.error("ip 체크하다 오류", e);
			return false;
		}
	}

	public boolean isPrivateInstance(HttpServletRequest request) {
		String ip = getIp(request);
		return instanceRanges.ipv4List.stream().anyMatch(s -> s.equals(ip) || s.contains(ip));
	}

	@ToString
	private static class ListRanges {
		private final Set<BigInteger[]> ranges = new HashSet<>();
		private final Set<String> ipv4List = new HashSet<>();

		public ListRanges(Set<String> list) {
			for (String str : list) {
				String[] cidr = str.split("/");
				if (cidr.length == 2) { // cidr
					String[] ipAddressInArray = cidr[0].split("\\.");
					int prefix = Integer.parseInt(cidr[1]);

					BigInteger ipVal = BigInteger.ZERO;
					for (int i = 0; i < ipAddressInArray.length; i++) {
						int power = 3 - i;
						int ipAddress = Integer.parseInt(ipAddressInArray[i]);
						ipVal = ipVal.add(BigInteger.valueOf(ipAddress).shiftLeft(power * 8));
					}

					BigInteger mask = BigInteger.ZERO.setBit(32).subtract(BigInteger.ONE).shiftRight(prefix);
					BigInteger[] range = new BigInteger[] {ipVal, mask};
					ranges.add(range);
				} else {
					ipv4List.add(str);
				}
			}
		}

		/**
		 * 주어진 IP 주소가 허용된 범위 내에 있는지 확인
		 *
		 * @param ipAddress 확인할 IP 주소
		 * @return 허용된 범위 내에 있으면 true, 그렇지 않으면 false
		 * @throws UnknownHostException IP 주소 파싱 중 오류 발생 시
		 */
		public boolean isAllowed(String ipAddress) throws UnknownHostException {
			if (!StringUtils.hasLength(ipAddress)) {
				return false;
			}

			for (String ipv4 : ipv4List) {
				if (ipv4.startsWith(ipAddress)) {
					return true;
				}
			}

			if (!ranges.isEmpty()) {
				BigInteger ip = new BigInteger(1, InetAddress.getByName(ipAddress).getAddress());
				for (BigInteger[] range : ranges) {
					BigInteger start = range[0];
					BigInteger end = start.add(range[1]);

					if (ip.compareTo(start) >= 0 && ip.compareTo(end) <= 0) {
						return true;
					}
				}
			}

			if (log.isDebugEnabled()) {
				log.debug("접근금지. ipAddress: [{}]", ipAddress);
			}

			return false;
		}
	}
}
