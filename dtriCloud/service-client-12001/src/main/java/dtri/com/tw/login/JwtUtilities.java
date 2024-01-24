package dtri.com.tw.login;

//@Slf4j
//@Component
//public class JwtUtilities {
//	private Logger log = LoggerFactory.getLogger(this.getClass());
//
//	@Value("${jwt.secret}")
//	private String secretKey;// 密鑰
//
//	@Value("${jwt.expiration}")
//	private Long jwtExpiration;// 時間
//
//	// 查看(令牌)內 取得帳號資訊
//	public String extractUsername(String token) {
//		return extractClaim(token, Claims::getSubject);
//	}
//
//	public Claims extractAllClaims(String token) {
//		return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
//	}
//
//	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//		final Claims claims = extractAllClaims(token);
//		return claimsResolver.apply(claims);
//	}
//
//	// 查看(令牌)內 取得有效時間
//	public Date extractExpiration(String token) {
//		return extractClaim(token, Claims::getExpiration);
//	}
//
//	// 查看(令牌)內帳號 與 伺服器帳號是否相同
//	public Boolean validateToken(String token, UserDetails userDetails) {
//		final String userAccount = extractUsername(token);
//		return (userAccount.equals(userDetails.getUsername()) && !isTokenExpired(token));
//	}
//
//	// 查看(令牌)是否過過期
//	public Boolean isTokenExpired(String token) {
//		return extractExpiration(token).before(new Date());
//	}
//
//	// 產生(令牌)加密(帳號/權限/建立時間/過期時間)
//	public String generateToken(String userAccount, List<String> roles) {
//		// 密鑰轉換->Base64編碼->寫入密鑰加密
//		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
//		return Jwts.builder().setSubject(userAccount).claim("role", roles)//
//				.setIssuedAt(new Date(System.currentTimeMillis()))// 啟用時間
//				.setExpiration(Date.from(Instant.now().plus(jwtExpiration, ChronoUnit.MILLIS)))// 有效時間
//				.signWith(SignatureAlgorithm.HS256, secretKey)// 與密鑰加密
//				.compact();
//	}
//
//	public boolean validateToken(String token) {
//		try {
//			Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
//			return true;
//		} catch (SignatureException e) {
//			log.info("Invalid JWT signature.");
//			log.trace("Invalid JWT signature trace: {}", e);
//		} catch (MalformedJwtException e) {
//			log.info("Invalid JWT token.");
//			log.trace("Invalid JWT token trace: {}", e);
//		} catch (ExpiredJwtException e) {
//			log.info("Expired JWT token.");
//			log.trace("Expired JWT token trace: {}", e);
//		} catch (UnsupportedJwtException e) {
//			log.info("Unsupported JWT token.");
//			log.trace("Unsupported JWT token trace: {}", e);
//		} catch (IllegalArgumentException e) {
//			log.info("JWT token compact of handler are invalid.");
//			log.trace("JWT token compact of handler are invalid trace: {}", e);
//		}
//		return false;
//	}
//
//	// 取得部分資料(尚未街溪透)
//	public String getToken(HttpServletRequest httpServletRequest) {
//		final String bearerToken = httpServletRequest.getHeader("Authorization");
//		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//			return bearerToken.substring(7, bearerToken.length());
//		} // The part after "Bearer "
//		return null;
//	}
//
//}