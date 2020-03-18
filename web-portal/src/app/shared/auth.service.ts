import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable, Subject } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { throwError } from "rxjs";
import { User } from "./user.model";
import { Router } from "@angular/router";

const baseUrl = '/api/security';

export interface AuthResponse {
  access_token: string,
  token_type: string,
  refresh_token: string,
  expires_in: number,
  scope: string,
  jti: string,

}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  user = new Subject<User>();

  constructor(private http: HttpClient, private router: Router) {
  }

  login(username: string, password: string): Observable<AuthResponse> {
    const formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);
    formData.append('grant_type', 'password');
    const headers = {
      'Authorization': `Basic ${btoa('dwh-client:secret')}`,
    };
    return this.http
      .post<AuthResponse>(`${baseUrl}/oauth/token`, formData, { headers })
      .pipe(
        catchError(this.handleError.bind(this)),
        tap(this.handleAuthentication.bind(this)),
      );
  }

  public isAuthenticated(): boolean {
    return sessionStorage.getItem("token") !== null;
  }

  public token(): string {
    return JSON.parse(sessionStorage.getItem("token")).access_token;
  }

  public logout() {
    sessionStorage.removeItem('token');
    this.user.next(null);
    this.router.navigate(['/login']);
  }

  public userInfo(): Observable<any> {
    return this.http
      .get<any>(`${baseUrl}/me`)
      .pipe(
        catchError(this.handleError.bind(this)),
      );
  }

  private handleAuthentication(auth: AuthResponse) {
    sessionStorage.setItem('token', JSON.stringify(auth));
    const user = new User('Admin', auth.jti, auth.access_token, new Date().getMilliseconds());
    this.user.next(user);
  }

  private handleError(error) {
    console.log(error);
    let errorMessage = 'Error occured';
    if (!error.error || !error.error.error) {
      return throwError(errorMessage);
    }
    switch (error.error.error) {
      case 'invalid_grant':
        errorMessage = 'Invalid username or password'
    }
    return throwError(errorMessage);
  };
}
