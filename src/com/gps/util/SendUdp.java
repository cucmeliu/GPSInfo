package com.gps.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SendUdp {

	public DatagramSocket socket;
	InetAddress serverAddress;
	int port;

	public SendUdp(String str) {
		try {
			socket = new DatagramSocket(2020);
			updateAddress(str);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void updateAddress(String str) {
		String[] strs = str.split(":");
		port = Integer.parseInt(strs[1].trim());
		String ipAddress = strs[0].trim();
		try {
			serverAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void connectServerWithUDPSocket(String str) {

		try {
			byte data[] = str.getBytes();
			DatagramPacket packet = new DatagramPacket(data, data.length,
					serverAddress, port);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
