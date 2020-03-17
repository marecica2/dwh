import { Injectable } from "@angular/core";
import { HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpParams, HttpRequest } from "@angular/common/http";
import { Observable } from "rxjs";
import { exhaustMap, take } from "rxjs/operators";

import { AuthService } from "./auth.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.url.indexOf("/oauth/token") !== -1) {
      return next.handle(req);
    }
    return this.authService.user.pipe(
      take(1),
      exhaustMap(user => {
        console.log("interceptor user", user);
        const headers = new HttpHeaders();
        headers.set('Authorization', `Bearer ${user.token}`);
        const modifiedReq = req.clone({ headers });
        return next.handle(modifiedReq);
      })
    );
  }

}
