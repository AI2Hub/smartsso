package openjoe.smart.sso.server.controller;

import openjoe.smart.sso.base.constant.BaseConstant;
import openjoe.smart.sso.base.entity.Result;
import openjoe.smart.sso.base.entity.Token;
import openjoe.smart.sso.base.entity.TokenUser;
import openjoe.smart.sso.base.enums.GrantTypeEnum;
import openjoe.smart.sso.server.entity.CodeContent;
import openjoe.smart.sso.server.entity.TokenContent;
import openjoe.smart.sso.server.manager.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Oauth2服务管理
 *
 * @author Joe
 */
@SuppressWarnings("rawtypes")
@RestController
@RequestMapping(BaseConstant.AUTH_PATH)
public class Oauth2Controller {

    @Autowired
    private AppManager appManager;
    @Autowired
    private UserManager userManager;

    @Autowired
    private AbstractCodeManager codeManager;
    @Autowired
    private AbstractTokenManager tokenManager;
    @Autowired
    private AbstractTicketGrantingTicketManager ticketGrantingTicketManager;

    /**
     * 获取accessToken
     *
     * @param appKey
     * @param appSecret
     * @param code
     * @return
     */
    @RequestMapping(value = "/access_token", method = RequestMethod.GET)
    public Result getAccessToken(
            @RequestParam(value = BaseConstant.GRANT_TYPE) String grantType,
            @RequestParam(value = BaseConstant.APP_KEY) String appKey,
            @RequestParam(value = BaseConstant.APP_SECRET) String appSecret,
            @RequestParam(value = BaseConstant.AUTH_CODE) String code) {

        // 校验授权码方式
        if (!GrantTypeEnum.AUTHORIZATION_CODE.getValue().equals(grantType)) {
            return Result.error("仅支持授权码方式");
        }

        // 校验应用
        Result<Void> appResult = appManager.validate(appKey, appSecret);
        if (!appResult.isSuccess()) {
            return appResult;
        }

        // 校验授权码
        CodeContent codeContent = codeManager.get(code);
        if (codeContent == null) {
            return Result.error("code有误或已过期");
        }
        codeManager.remove(code);

        // 校验凭证
        TokenUser tokenUser = ticketGrantingTicketManager.get(codeContent.getTgt());
        if (tokenUser == null) {
            return Result.error("服务端TGT已过期");
        }

        // 创建token
        TokenContent tc = tokenManager.create(tokenUser, appKey, codeContent);

        // 刷新服务端凭证时效
        ticketGrantingTicketManager.refresh(tc.getTgt());

        // 返回token
        return Result.success(new Token(tc.getAccessToken(), tokenManager.getAccessTokenTimeout(), tc.getRefreshToken(),
                tokenManager.getRefreshTokenTimeout(), tc.getTokenUser()));
    }

    /**
     * 刷新accessToken，并延长TGT超时时间
     *
     * @param appKey
     * @param refreshToken
     * @return
     */
    @RequestMapping(value = "/refresh_token", method = RequestMethod.GET)
    public Result getRefreshToken(
            @RequestParam(value = BaseConstant.APP_KEY) String appKey,
            @RequestParam(value = BaseConstant.REFRESH_TOKEN) String refreshToken) {
        if (!appManager.exists(appKey)) {
            return Result.error("非法应用");
        }

        TokenContent atContent = tokenManager.get(refreshToken);
        if (atContent == null) {
            return Result.error("refreshToken有误或已过期");
        }

        // 删除原有token
        tokenManager.remove(refreshToken);

        // 创建新token
        TokenContent tc = tokenManager.create(atContent);

        // 刷新服务端凭证时效
        ticketGrantingTicketManager.refresh(tc.getTgt());

        // 返回新token
        return Result.success(new Token(tc.getAccessToken(), tokenManager.getAccessTokenTimeout(), tc.getRefreshToken(),
                tokenManager.getRefreshTokenTimeout(), tc.getTokenUser()));
    }
}