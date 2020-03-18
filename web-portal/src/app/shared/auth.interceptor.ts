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
    let headers = new HttpHeaders();
    headers = headers.append('Authorization', `Bearer ${this.authService.token()}`);
    const modifiedReq = req.clone({ headers });
    return next.handle(modifiedReq);
  }

}
