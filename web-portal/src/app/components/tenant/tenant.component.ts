import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgForm } from "@angular/forms";
import { Subscription } from "rxjs";
import { AuthService } from "../../shared/auth.service";
import { TenantService } from "./tenant.service";

@Component({
  selector: 'app-tenant',
  templateUrl: './tenant.component.html',
  styleUrls: ['./tenant.component.css']
})
export class TenantComponent implements OnInit, OnDestroy {
  isLoading: false;
  tenants = [];
  private tenantSub: Subscription;

  constructor(private authService: AuthService, private tenantService: TenantService) {
  }

  ngOnInit(): void {
    this.tenantSub = this.authService.userInfo().subscribe(user => {
      console.log(user);
      this.tenants = user.tenants;
    });
  }

  ngOnDestroy(): void {
    this.tenantSub.unsubscribe();
  }

  onSubmit({ value }) {
    const selectedTenant = this.tenants
      .find(tenant => tenant.id === value);
    this.tenantService.selectTenant(selectedTenant);
  }

}
