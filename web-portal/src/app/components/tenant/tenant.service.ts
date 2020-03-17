import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { from, Observable, Subject } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { throwError } from "rxjs";
import { Router } from "@angular/router";
import { Tenant } from "../../shared/tenant.model";

const baseUrl = '/api/security/tenants/admin';


@Injectable({
  providedIn: 'root'
})
export class TenantService {
  user = new Subject<Tenant>();

  constructor(private http: HttpClient, private router: Router) {
  }

  getTenants(): Observable<any>{
    return from([[{ name: 'tenant 1', id: '00000-00000-00001' }, { name: 'tenant 2', id: '00000-00000-00002'}]]);
    // return this.http
    //   .get<Tenant>(baseUrl)
    //   .pipe(
    //     catchError(this.handleError.bind(this)),
    //   );
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

  public isAuthenticated(): boolean {
    return sessionStorage.getItem("token") !== null;
  }

  public logout() {
    sessionStorage.removeItem('token');
    this.user.next(null);
    this.router.navigate(['/login']);
  }
}
