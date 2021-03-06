/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package cc.blynk.core.http.handlers;

import cc.blynk.core.http.AuthHeadersBaseHttpHandler;
import cc.blynk.core.http.Response;
import cc.blynk.server.Holder;
import cc.blynk.server.core.dao.SessionDao;
import cc.blynk.server.core.dao.TokenManager;
import cc.blynk.server.core.dao.TokenValue;
import cc.blynk.server.core.dao.UserDao;
import cc.blynk.server.core.dao.UserKey;
import cc.blynk.server.core.dao.ota.OTAInfo;
import cc.blynk.server.core.dao.ota.OTAManager;
import cc.blynk.server.core.model.auth.Session;
import cc.blynk.server.core.model.auth.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static cc.blynk.core.http.Response.badRequest;
import static cc.blynk.core.http.Response.ok;
import static cc.blynk.server.core.protocol.enums.Command.BLYNK_INTERNAL;

public class OTAHandler extends UploadHandler {

    private static final Logger log = LogManager.getLogger(OTAHandler.class);

    private final TokenManager tokenManager;
    private final SessionDao sessionDao;
    private final UserDao userDao;
    private QueryStringDecoder queryStringDecoder;
    private final OTAManager otaManager;

    public OTAHandler(Holder holder, String handlerUri, String uploadFolder) {
        super(holder.props.jarPath, handlerUri, uploadFolder);
        this.tokenManager = holder.tokenManager;
        this.sessionDao = holder.sessionDao;
        this.userDao = holder.userDao;
        this.otaManager = holder.otaManager;
    }

    @Override
    public boolean accept(ChannelHandlerContext ctx, HttpRequest req) {
        if (req.method() == HttpMethod.POST && req.uri().startsWith(handlerUri)) {
            try {
                User superAdmin = AuthHeadersBaseHttpHandler.validateAuth(userDao, req);
                if (superAdmin != null) {
                    ctx.channel().attr(AuthHeadersBaseHttpHandler.USER).set(superAdmin);
                    queryStringDecoder = new QueryStringDecoder(req.uri());
                    return true;
                }
            } catch (IllegalAccessException e) {
                //return 403 and stop processing.
                ctx.writeAndFlush(Response.forbidden(e.getMessage()));
                return true;
            }
        }
        return false;
    }

    @Override
    public Response afterUpload(ChannelHandlerContext ctx, String pathToFirmware) {
        String token = getParam("token");
        if (token != null) {
            log.info("Requested OTA for single device {}.", token);
            return singleDeviceOTA(ctx, token, pathToFirmware);
        }

        String user = getParam("user");
        if (user != null) {
            String appName = getParam("appName");
            UserKey userKey = new UserKey(user, appName);
            String project = getParam("project");
            log.info("Requested OTA for single user {}. Project {}.", user, project);
            return singleUserOTA(ctx, userKey, project, pathToFirmware);
        }

        log.info("Requested OTA for all devices...");
        return allDevicesOTA(ctx, pathToFirmware);
    }

    private String getParam(String paramString) {
        List<String> param = queryStringDecoder.parameters().get(paramString);
        if (param == null) {
            return null;
        }
        return param.get(0);
    }

    private Response allDevicesOTA(ChannelHandlerContext ctx, String pathToFirmware) {
        User initiator = ctx.channel().attr(AuthHeadersBaseHttpHandler.USER).get();
        otaManager.initiateForAll(initiator, pathToFirmware);

        return ok(pathToFirmware);
    }

    private Response singleUserOTA(ChannelHandlerContext ctx, UserKey userKey,
                                   String projectName, String pathToFirmware) {
        User initiator = ctx.channel().attr(AuthHeadersBaseHttpHandler.USER).get();
        User user = userDao.users.get(userKey);

        if (user == null) {
            log.info("Requested user {} not found.", userKey);
            return badRequest("Requested user not found.");
        }

        otaManager.initiate(initiator, userKey, projectName, pathToFirmware);

        return ok(pathToFirmware);
    }

    private Response singleDeviceOTA(ChannelHandlerContext ctx, String token, String pathToFirmware) {
        TokenValue tokenValue = tokenManager.getTokenValueByToken(token);

        if (tokenValue == null) {
            log.debug("Requested token {} not found.", token);
            return badRequest("Invalid token.");
        }

        User user = tokenValue.user;
        int dashId = tokenValue.dash.id;
        int deviceId = tokenValue.device.id;

        Session session = sessionDao.get(new UserKey(user));
        if (session == null) {
            log.debug("No session for user {}.", user.email);
            return badRequest("Device wasn't connected yet.");
        }

        String body = OTAInfo.makeHardwareBody(otaManager.serverHostUrl, pathToFirmware);
        if (session.sendMessageToHardware(dashId, BLYNK_INTERNAL, 7777, body, deviceId)) {
            log.debug("No device in session.");
            return badRequest("No device in session.");
        }

        User initiator = ctx.channel().attr(AuthHeadersBaseHttpHandler.USER).get();
        if (initiator != null) {
            tokenValue.device.updateOTAInfo(initiator.email);
        }

        return ok(pathToFirmware);
    }

}
