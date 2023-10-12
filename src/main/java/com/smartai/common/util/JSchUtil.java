package com.smartai.common.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class JSchUtil {

    /**
     * 连接到SSH服务器
     *
     * @param host 主机名
     * @param port 端口号
     * @param username 用户名
     * @param password 密码
     * @return
     */
    public static Session connect(String host, int port, String username, String password) {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
        } catch (JSchException e) {
            log.error("创建session异常：{}", e);
            throw new RuntimeException("创建session异常", e);
        }
        return session;
    }

    /**
     * 检测端口是否可用
     *
     * @param session
     * @param port
     * @return
     */
    public static boolean isOccupied(Session session, int port) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            String command = "lsof -i:" + port;
            channel.setCommand(command);
            channel.connect();
            byte[] buffer = new byte[1024];
            InputStream in = channel.getInputStream();
            int len = in.read(buffer);
            if (len != -1) {
                String output = new String(buffer, 0, len, "UTF-8");
                return output.contains("LISTEN");
            }
        } catch (JSchException | IOException e) {
            log.error("端口占用检测异常：{}", e);
            throw new RuntimeException("端口占用检测异常", e);
        } finally {
            if (Objects.nonNull(channel)) {
                channel.disconnect();
            }
        }
        return false;
    }

    /**
     * 服务进程是否存在
     *
     * @param session
     * @param fileName
     * @return
     */
    public static boolean processStatus(Session session, String fileName, String path) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            String command = "ps -ef | grep " + fileName;
            channel.setCommand(command);
            channel.connect();
            byte[] buffer = new byte[1024];
            InputStream in = channel.getInputStream();
            int len = in.read(buffer);
            if (len != -1) {
                String output = new String(buffer, 0, len, "UTF-8");
                return output.contains(path.concat(fileName));
            }
        } catch (JSchException | IOException e) {
            log.error("服务存活状态检测异常：{}", e);
            throw new RuntimeException("服务存活状态检测异常", e);
        } finally {
            if (Objects.nonNull(channel)) {
                channel.disconnect();
            }
        }
        return false;
    }

    /**
     * 执行shell命令
     *
     * @param session
     * @param command
     * @return
     */
    public static String execCommand(Session session, String command) {
        ChannelExec channelExec = null;
        String successResult = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();
            successResult = getResult(channelExec.getInputStream());
            return successResult;
        } catch (JSchException | IOException e) {
            log.error("命令执行异常：{}", e);
            throw new RuntimeException("命令执行异常", e);
        } finally {
            if (Objects.nonNull(channelExec)) {
                channelExec.disconnect();
            }
        }
    }

    private static String getResult(InputStream inputStream) {
        StringBuilder result = new StringBuilder();
        try (InputStream in = inputStream;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (Exception e) {
            log.error("exception:{}", e);
        }
        return result.toString();
    }

    /**
     * 上传文件
     *
     * @param session
     * @param localFilePath 本地文件路径
     * @param remoteFilePath 远程文件路径
     */
    public static void upload(Session session, String localFilePath, String remoteFilePath) {
        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.put(localFilePath, remoteFilePath);
        } catch (JSchException | SftpException e) {
            log.error("文件上传异常：{}", e);
            throw new RuntimeException("文件上传异常", e);
        } finally {
            if (Objects.nonNull(channelSftp)) {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * 上传文件
     *
     * @param session
     * @param inputStream
     * @param remoteFilePath
     */
    public static void upload(Session session, InputStream inputStream, String remoteFilePath) {
        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.put(inputStream, remoteFilePath);
        } catch (JSchException | SftpException e) {
            log.error("文件上传异常：{}", e);
            throw new RuntimeException("文件上传异常", e);
        } finally {
            if (Objects.nonNull(channelSftp)) {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * 下载文件
     *
     * @param session
     * @param remoteFilePath 远程文件路径
     * @param localFilePath 本地文件路径
     */
    public static void download(Session session, String remoteFilePath, String localFilePath) {
        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.get(remoteFilePath, localFilePath);
        } catch (JSchException | SftpException e) {
            log.error("文件下载异常：{}", e);
            throw new RuntimeException("文件下载异常", e);
        } finally {
            if (Objects.nonNull(channelSftp)) {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * 关闭SSH连接
     *
     * @param session
     */
    public static void disconnect(Session session) {
        if (session != null) {
            session.disconnect();
        }
    }
}
