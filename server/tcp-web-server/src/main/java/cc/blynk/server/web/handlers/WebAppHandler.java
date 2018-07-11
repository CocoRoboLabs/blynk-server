package cc.blynk.server.web.handlers;

import cc.blynk.server.Holder;
import cc.blynk.server.common.BaseSimpleChannelInboundHandler;
import cc.blynk.server.common.handlers.logic.PingLogic;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import cc.blynk.server.core.session.StateHolderBase;
import cc.blynk.server.core.stats.GlobalStats;
import cc.blynk.server.web.handlers.logic.GetWebGraphDataLogic;
import cc.blynk.server.web.handlers.logic.ResolveWebEventLogic;
import cc.blynk.server.web.handlers.logic.WebAppHardwareLogic;
import cc.blynk.server.web.handlers.logic.account.GetAccountLogic;
import cc.blynk.server.web.handlers.logic.account.UpdateAccountLogic;
import cc.blynk.server.web.handlers.logic.device.TrackDeviceLogic;
import cc.blynk.server.web.handlers.logic.device.WebCreateDeviceLogic;
import cc.blynk.server.web.handlers.logic.device.WebGetDeviceLogic;
import cc.blynk.server.web.handlers.logic.device.WebGetDevicesLogic;
import cc.blynk.server.web.handlers.logic.device.WebUpdateDeviceLogic;
import cc.blynk.server.web.handlers.logic.organization.CanInviteUserLogic;
import cc.blynk.server.web.handlers.logic.organization.WebGetOrganizationLocationsLogic;
import cc.blynk.server.web.handlers.logic.organization.WebGetOrganizationLogic;
import cc.blynk.server.web.handlers.logic.organization.WebGetOrganizationUsersLogic;
import cc.blynk.server.web.handlers.logic.organization.WebGetOrganizationsLogic;
import cc.blynk.server.web.handlers.logic.product.WebCreateProductLogic;
import cc.blynk.server.web.handlers.logic.product.WebDeleteProductLogic;
import cc.blynk.server.web.handlers.logic.product.WebGetProductLogic;
import cc.blynk.server.web.handlers.logic.product.WebGetProductsLogic;
import cc.blynk.server.web.handlers.logic.product.WebUpdateDevicesMetaInProductLogic;
import cc.blynk.server.web.handlers.logic.product.WebUpdateProductLogic;
import cc.blynk.server.web.session.WebAppStateHolder;
import io.netty.channel.ChannelHandlerContext;

import static cc.blynk.server.core.protocol.enums.Command.GET_ENHANCED_GRAPH_DATA;
import static cc.blynk.server.core.protocol.enums.Command.HARDWARE;
import static cc.blynk.server.core.protocol.enums.Command.PING;
import static cc.blynk.server.core.protocol.enums.Command.RESOLVE_EVENT;
import static cc.blynk.server.core.protocol.enums.Command.TRACK_DEVICE;
import static cc.blynk.server.core.protocol.enums.Command.WEB_CAN_INVITE_USER;
import static cc.blynk.server.core.protocol.enums.Command.WEB_CREATE_DEVICE;
import static cc.blynk.server.core.protocol.enums.Command.WEB_CREATE_PRODUCT;
import static cc.blynk.server.core.protocol.enums.Command.WEB_DELETE_PRODUCT;
import static cc.blynk.server.core.protocol.enums.Command.WEB_GET_ACCOUNT;
import static cc.blynk.server.core.protocol.enums.Command.WEB_GET_DEVICE;
import static cc.blynk.server.core.protocol.enums.Command.WEB_GET_DEVICES;
import static cc.blynk.server.core.protocol.enums.Command.WEB_GET_ORG;
import static cc.blynk.server.core.protocol.enums.Command.WEB_GET_ORGS;
import static cc.blynk.server.core.protocol.enums.Command.WEB_GET_ORG_LOCATIONS;
import static cc.blynk.server.core.protocol.enums.Command.WEB_GET_ORG_USERS;
import static cc.blynk.server.core.protocol.enums.Command.WEB_GET_PRODUCT;
import static cc.blynk.server.core.protocol.enums.Command.WEB_GET_PRODUCTS;
import static cc.blynk.server.core.protocol.enums.Command.WEB_UPDATE_ACCOUNT;
import static cc.blynk.server.core.protocol.enums.Command.WEB_UPDATE_DEVICE;
import static cc.blynk.server.core.protocol.enums.Command.WEB_UPDATE_DEVICES_META_IN_PRODUCT;
import static cc.blynk.server.core.protocol.enums.Command.WEB_UPDATE_PRODUCT;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 *
 */
public class WebAppHandler extends BaseSimpleChannelInboundHandler<StringMessage> {

    public final WebAppStateHolder state;
    private final WebAppHardwareLogic webAppHardwareLogic;
    private final GetWebGraphDataLogic getWebGraphDataLogic;
    private final ResolveWebEventLogic resolveWebEventLogic;
    private final WebCreateDeviceLogic webCreateDeviceLogic;
    private final WebGetDevicesLogic webGetDevicesLogic;
    private final WebGetDeviceLogic webGetDeviceLogic;
    private final WebGetOrganizationLogic webGetOrganizationLogic;
    private final WebGetOrganizationsLogic webGetOrganizationsLogic;
    private final WebGetOrganizationUsersLogic webGetOrganizationUsersLogic;
    private final WebGetOrganizationLocationsLogic webGetOrganizationLocationsLogic;
    private final CanInviteUserLogic canInviteUserLogic;
    private final WebCreateProductLogic webCreateProductLogic;
    private final WebGetProductLogic webGetProductLogic;
    private final WebGetProductsLogic webGetProductsLogic;
    private final WebUpdateProductLogic webUpdateProductLogic;
    private final WebDeleteProductLogic webDeleteProductLogic;
    private final WebUpdateDeviceLogic webUpdateDeviceLogic;
    private final WebUpdateDevicesMetaInProductLogic webUpdateDevicesMetaInProductLogic;

    private final GlobalStats stats;

    public WebAppHandler(Holder holder, WebAppStateHolder state) {
        super(StringMessage.class);
        this.webAppHardwareLogic = new WebAppHardwareLogic(holder);
        this.getWebGraphDataLogic = new GetWebGraphDataLogic(holder);
        this.resolveWebEventLogic = new ResolveWebEventLogic(holder);
        this.webCreateDeviceLogic = new WebCreateDeviceLogic(holder);
        this.webGetDevicesLogic = new WebGetDevicesLogic(holder);
        this.webGetDeviceLogic = new WebGetDeviceLogic(holder);
        this.webGetOrganizationLogic = new WebGetOrganizationLogic(holder);
        this.webGetOrganizationsLogic = new WebGetOrganizationsLogic(holder);
        this.webGetOrganizationUsersLogic = new WebGetOrganizationUsersLogic(holder);
        this.webGetOrganizationLocationsLogic = new WebGetOrganizationLocationsLogic(holder);
        this.canInviteUserLogic = new CanInviteUserLogic(holder);
        this.webCreateProductLogic = new WebCreateProductLogic(holder);
        this.webGetProductLogic = new WebGetProductLogic(holder);
        this.webGetProductsLogic = new WebGetProductsLogic(holder);
        this.webUpdateProductLogic = new WebUpdateProductLogic(holder);
        this.webDeleteProductLogic = new WebDeleteProductLogic(holder);
        this.webUpdateDeviceLogic = new WebUpdateDeviceLogic(holder);
        this.webUpdateDevicesMetaInProductLogic = new WebUpdateDevicesMetaInProductLogic(holder);

        this.state = state;
        this.stats = holder.stats;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, StringMessage msg) {
        this.stats.incrementAppStat();
        switch (msg.command) {
            case WEB_GET_ACCOUNT:
                GetAccountLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_UPDATE_ACCOUNT:
                UpdateAccountLogic.messageReceived(ctx, state, msg);
                break;
            case HARDWARE :
                webAppHardwareLogic.messageReceived(ctx, state, msg);
                break;
            case TRACK_DEVICE :
                TrackDeviceLogic.messageReceived(ctx, state, msg);
                break;
            case GET_ENHANCED_GRAPH_DATA :
                getWebGraphDataLogic.messageReceived(ctx, state, msg);
                break;
            case RESOLVE_EVENT :
                resolveWebEventLogic.messageReceived(ctx, state, msg);
                break;
            case PING :
                PingLogic.messageReceived(ctx, msg.id);
                break;
            case WEB_CREATE_DEVICE :
                webCreateDeviceLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_UPDATE_DEVICE :
                webUpdateDeviceLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_GET_DEVICES :
                webGetDevicesLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_GET_DEVICE :
                webGetDeviceLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_GET_ORG :
                webGetOrganizationLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_GET_ORGS :
                webGetOrganizationsLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_GET_ORG_USERS :
                webGetOrganizationUsersLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_GET_ORG_LOCATIONS :
                webGetOrganizationLocationsLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_CAN_INVITE_USER :
                canInviteUserLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_CREATE_PRODUCT :
                webCreateProductLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_GET_PRODUCT :
                webGetProductLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_GET_PRODUCTS :
                webGetProductsLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_UPDATE_PRODUCT :
                webUpdateProductLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_DELETE_PRODUCT :
                webDeleteProductLogic.messageReceived(ctx, state, msg);
                break;
            case WEB_UPDATE_DEVICES_META_IN_PRODUCT :
                webUpdateDevicesMetaInProductLogic.messageReceived(ctx, state, msg);
                break;
        }
    }

    @Override
    public StateHolderBase getState() {
        return state;
    }
}
