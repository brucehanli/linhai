package com.bsit.linhai605.utils;

//import android.text.TextUtils;

import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;


public class EncryptUtils {

	private final static String ALGORITHM = "DES";

	private static boolean isEmpty(String str){
		return str == null || str.trim().equals("");
	}

	/**
	 * 琴岛通计算MAC算法
	 * 
	 * @param source
	 *            源字符串
	 * @param key
	 *            密钥
	 * @return
	 */
	public static String calculateMac(String source, String key,
			String initDataStr) {
		if(isEmpty(source) || isEmpty(key)){
			return "source data or key data can't be null!";
		}
		for (int i = 0; i < source.length(); i++) {
			int asc = (int) source.charAt(i);
			// 如果源字符串不是0~9或者A~F的字符,返回错误信息
			if (!((asc >= 48 && asc <= 57) || (asc >= 65 && asc <= 70))) {
				return "source data can only be 0~9 A~F !";
			}
		}
		for (int i = 0; i < key.length(); i++) {
			int asc = (int) key.charAt(i);
			// 如果源字符串不是0~9或者A~F的字符,返回错误信息
			if (!((asc >= 48 && asc <= 57) || (asc >= 65 && asc <= 70))) {
				return "key data can only be 0~9 A~F !";
			}
		}

		if (32 != key.length()) {
			return "key length error! need 32, actually :" + key.length();
		}
		if (8 != initDataStr.length()) {
			return "init data length must be 8!";
		}

		int remainder = source.length() % 16;

		if (remainder != 0) {
			int length = 16 - remainder;
			if (length != 0) {
				for (int i = 0; i < length; i++) {
					if (0 == i) {
						source += "8";
					} else {
						source += "0";
					}
				}
			}
		}

		int times = source.length() / 16;

		// 加密密钥BCD码
		byte[] macKeyBcd = CodeConvertUtils.ASCII_To_BCD(key.getBytes());
		// 加密密钥前8位
		byte[] keyLeft = distributeBytes(macKeyBcd, 0);
		// 加密密钥后8位
		byte[] keyRight = distributeBytes(macKeyBcd, 1);
		// 待加密数据明文对应的BCD码
		byte[] sourceDataBcd = CodeConvertUtils.ASCII_To_BCD(source.getBytes());
		byte[] initData = initDataStr.getBytes();

		for (int i = 0; i < times; i++) {
			initData = xor(initData, sourceDataBcd, i);
			initData = ecbEncrypt(initData, keyLeft);
		}
		initData = ecbDecrypt(initData, keyRight);
		initData = ecbEncrypt(initData, keyLeft);

		String outString = CodeConvertUtils.BCD_To_Str(initData);

		return outString.substring(0, 8);
	}

	/**
	 * 计算MAC算法
	 * 
	 * @param source
	 *            源字符串
	 * @param key
	 *            密钥
	 * @return
	 */
	public static String calculateMac(String source, String key) {
		if(isEmpty(source) || isEmpty(key)){
			return "source data or key data can't be null!";
		}
		if (32 != key.length()) {
			return "key length error! need:32, actural:" + key.length();
		}
		/*
		 * if (0 != (source.length() % 16)) { return "source length error!"; }
		 */
		source = fixSourceData(source);

		// 待加密数据明文对应的BCD码
		byte[] sourceBt = CodeConvertUtils.ASCII_To_BCD(source.getBytes());
		// 加密密钥BCD码
		byte[] keyBt = CodeConvertUtils.ASCII_To_BCD(key.getBytes());

		return calculateMac(sourceBt, keyBt);
	}

	/**
	 * 计算MAC算法
	 * 
	 * @param source
	 *            源字符串
	 * @param key
	 *            密钥
	 * @return
	 */
	public static String calculateMac(byte[] source, byte[] key) {
		if (16 != key.length) {
			return "key length error! need:32, actural:" + key.length;
		}
		if (0 != (source.length % 8)) {
			return "source length error!";
		}

		int times = source.length / 8;
		// 加密密钥前8位
		byte[] keyLeft = EncryptUtils.distributeBytes(key, 0);
		// 加密密钥后8位
		byte[] keyRight = EncryptUtils.distributeBytes(key, 1);
		byte[] initData = { 0, 0, 0, 0, 0, 0, 0, 0 };

		for (int i = 0; i < times; i++) {
			initData = EncryptUtils.xor(initData, source, i);
			initData = EncryptUtils.ecbEncrypt(initData, keyLeft);
		}
		initData = EncryptUtils.ecbDecrypt(initData, keyRight);
		initData = EncryptUtils.ecbEncrypt(initData, keyLeft);

		String outString = CodeConvertUtils.BCD_To_Str(initData);

		return outString.substring(0, 8);
	}

	/**
	 * 加密
	 * 
	 * @param datasource
	 *            byte[]
	 * @param password
	 *            String
	 * @return byte[]
	 */
	public static byte[] ecbEncrypt(byte[] datasource, byte[] password) {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(datasource);
			DESKeySpec desKey = new DESKeySpec(password);
			// 创建一个密匙工厂，然后用它把DESKeySpec转换成
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance(ALGORITHM);
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			// 现在，获取数据并加密
			// 正式执行加密操作
			return cipher.doFinal(datasource);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解密
	 * 
	 * @param src
	 *            byte[]
	 * @param password
	 *            String
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] ecbDecrypt(byte[] src, byte[] password) {
		byte[] a = {};
		Cipher cipher = null;
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(src);
			// 创建一个DESKeySpec对象
			DESKeySpec desKey = new DESKeySpec(password);
			// 创建一个密匙工厂
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance(ALGORITHM);
			// 将DESKeySpec对象转换成SecretKey对象
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher对象实际完成解密操作
			cipher = Cipher.getInstance("DES/ECB/NoPadding");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, securekey, random);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 真正开始解密操作
		try {
			a = cipher.doFinal(src);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e1) {
			e1.printStackTrace();
		}
		return a;
	}

	/**
	 * 把by按照8字节数组进行拆分
	 */
	public static byte[] distributeBytes(byte[] by, int j) {
		byte[] b = new byte[8];
		for (int i = 0; i < 8; i++) {
			b[i] = by[i + j * 8];
		}
		return b;
	}

	/**
	 * 将a与b进行按位异或
	 */
	public static byte[] xor(byte[] a, byte[] b, int j) {
		byte[] rb = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			rb[i] = (byte) (a[i] ^ b[j * 8 + i]);
		}
		return rb;
	}

	/**
	 * 3des加密
	 */
	public static byte[] tripledes(byte[] sourceData, byte[] keyBcd) {
		// 解密密钥BCD码前8位
		byte[] keyBcdOne = distributeBytes(keyBcd, 0);
		// 解密密钥BCD码后8位
		byte[] keyBcdTwo = distributeBytes(keyBcd, 1);

		byte[] tempStr1 = ecbEncrypt(sourceData, keyBcdOne);
		byte[] tempStr2 = ecbDecrypt(tempStr1, keyBcdTwo);
		byte[] tempStr3 = ecbEncrypt(tempStr2, keyBcdOne);

		return tempStr3;
	}

	/**
	 * 3des解密
	 */
	public static byte[] tripleUndes(byte[] keySet, byte[] keyBcd) {
		// 解密密钥BCD码前8位
		byte[] keyBcdOne = distributeBytes(keyBcd, 0);
		// 解密密钥BCD码后8位
		byte[] keyBcdTwo = distributeBytes(keyBcd, 1);

		byte[] tempStr1 = ecbDecrypt(keySet, keyBcdOne);
		byte[] tempStr2 = ecbEncrypt(tempStr1, keyBcdTwo);
		byte[] tempStr3 = ecbDecrypt(tempStr2, keyBcdOne);

		return tempStr3;
	}

	/**
	 * 
	 * 对源数据补足8字节的整数倍
	 * 
	 * @author zhufeng.zhang
	 * @param sourceStr
	 * @return
	 * 
	 */
	private static String fixSourceData(String sourceStr) {
		int remainder = sourceStr.length() % 16;
		StringBuilder sb = new StringBuilder(sourceStr);
		if (remainder != 0) {
			int length = 16 - remainder;
			if (length != 0) {
				for (int i = 0; i < length; i++) {
					if (0 == i) {
						sb.append("8");
					} else {
						sb.append("0");
					}
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 对源字节数组补足8字节的整数倍
	 * 
	 * @param dataBt
	 */
	public static byte[] fixSourceDataBt(byte[] dataBt) {
		int remainder = dataBt.length % 8;
		if (remainder != 0) {
			int length = 8 - remainder;
			if (length != 0) {
				for (int i = 0; i < length; i++) {
					if (0 == i) {
						dataBt = CodeConvertUtils.joinByteBToBytesAEnd(dataBt,
								new byte[] { (byte) 0x80 });
					} else {
						dataBt = CodeConvertUtils.joinByteBToBytesAEnd(dataBt,
								new byte[] { 0x00 });
					}
				}
			}
		}
		return dataBt;
	}

	/**
	 * 加密函数
	 * 
	 * @param data
	 *            加密数据
	 * @param key
	 *            密钥
	 * @return 返回加密后的数据
	 */
	public static byte[] cbcEncrypt(byte[] data, byte[] key, byte[] iv) {

		try {
			// 从原始密钥数据创建DESKeySpec对象
			DESKeySpec dks = new DESKeySpec(key);

			// 创建一个密匙工厂，然后用它把DESKeySpec转换成
			// 一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(dks);

			// Cipher对象实际完成加密操作
			// Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			// 若采用NoPadding模式，data长度必须是8的倍数
			Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");

			// 用密匙初始化Cipher对象
			IvParameterSpec param = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, param);

			// 执行加密操作
			byte encryptedData[] = cipher.doFinal(data);

			return encryptedData;
		} catch (Exception e) {
			System.err.println("DES算法，加密数据出错!");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 部位后的加密结果 解成原始字符串
	 *
	 * @param password 加密后的密码
	 * @param key 密钥
	 * @return
	 */
	public static String decode(String password,String key){
		byte[] bytes = tripleUndes(CodeConvertUtils.hexString2Bytes(password), CodeConvertUtils.ASCII_To_BCD(key.getBytes()));
		for (int i=bytes.length -1;i>=0;i--){
			if (bytes [i] == -128){
				bytes[i] = 0x00;
				break;
			}
		}
		return new String(bytes).trim();
	}

	/**
	 * 原始密码 加密成字符串
	 *
	 * @param initPassword 原始密码
	 * @param key 密钥
	 * @return
	 */
	public static String encode(String initPassword,String key){
		byte [] keyData = CodeConvertUtils.ASCII_To_BCD(key.getBytes());
		byte [] fixPsw = EncryptUtils.fixSourceDataBt(initPassword.getBytes());
		byte[] encResult = EncryptUtils.tripledes(fixPsw, keyData);
		return CodeConvertUtils.bytes2HexString(encResult);
	}


	/**
	 * 解密函数
	 * 
	 * @param data
	 *            解密数据
	 * @param key
	 *            密钥
	 * @return 返回解密后的数据
	 */
	public static byte[] cbcDecrypt(byte[] data, byte[] key, byte[] iv) {
		try {
			// 从原始密匙数据创建一个DESKeySpec对象
			DESKeySpec dks = new DESKeySpec(key);

			// 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
			// 一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(dks);

			// using DES in CBC mode
			// Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			// 若采用NoPadding模式，data长度必须是8的倍数
			Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");

			// 用密匙初始化Cipher对象
			IvParameterSpec param = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, param);

			// 正式执行解密操作
			byte decryptedData[] = cipher.doFinal(data);

			return decryptedData;
		} catch (Exception e) {
			System.err.println("DES算法，解密出错。");
			e.printStackTrace();
		}

		return null;
	}

}
