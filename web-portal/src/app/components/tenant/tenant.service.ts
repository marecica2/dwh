import { Injectable } from '@angular/core';
import { Router } from "@angular/router";
import { Tenant } from "../../shared/tenant.model";

@Injectable({
  providedIn: 'root'
})
export class TenantService {

  constructor(private router: Router) {
  }

  public selectTenant(tenant: Tenant) {
    sessionStorage.setItem('tenant', JSON.stringify(tenant));
    sessionStorage.setItem('project', JSON.stringify({ id: '1', name: 'Project 1'}));
    this.router.navigate(['/apps']);
  }
}
