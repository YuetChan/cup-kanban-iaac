import { Controller, Get, Req, Res, UseGuards } from '@nestjs/common';
import { JwtAuthService, LoginType } from '../jwt/jwt-auth.service';
import { GoogleOauthGuard } from './google-oauth.guard';

@Controller('oauth/google-oauth')
export class GoogleOauthController {

  UI_HOST = '';

  constructor(private jwtAuthSvc: JwtAuthService) { this.UI_HOST = process.env.UI_HOST; }

  @Get()
  @UseGuards(GoogleOauthGuard)
  async googleAuth(@Req() req) { 
    console.trace('Enter googleAuth(req)');
    return req; 
  }

  @Get('redirect')
  @UseGuards(GoogleOauthGuard)
  async googleAuthRedirect(@Req() req, @Res() res) { 
    console.trace('Enter googleAuthRedirect(req, res)')

    res.cookie('jwt', await this.jwtAuthSvc.getJwt(req.user, LoginType.GOOGLE));
    res.redirect(302, `${this.UI_HOST}`);
  }

}
