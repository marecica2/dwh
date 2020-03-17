import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgForm } from "@angular/forms";
import { TenantService } from "./tenant.service";
import { Subscription } from "rxjs";

@Component({
  selector: 'app-tenant',
  templateUrl: './tenant.component.html',
  styleUrls: ['./tenant.component.css']
})
export class TenantComponent implements OnInit, OnDestroy {
  isLoading: false;
  tenants = [];
  private tenantSub: Subscription;

  constructor(private tenantService: TenantService) {
  }

  ngOnInit(): void {
    this.tenantSub = this.tenantService.getTenants().subscribe(tenants => {
      console.log(tenants);
      this.tenants = tenants;
    });
  }

  ngOnDestroy(): void {
    this.tenantSub.unsubscribe();
  }

  onSubmit(form: NgForm) {
    console.log(form.value);
  }

}
