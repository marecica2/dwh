import { Component, OnInit } from '@angular/core';
import { Router } from "@angular/router";
import { NgForm } from "@angular/forms";
import { MatSnackBar } from '@angular/material/snack-bar';
import { LoginService, AuthResponse } from "./login.service";

@Component({
  selector: 'app-log-in',
  templateUrl: './log-in.component.html',
  styleUrls: ['./log-in.component.css']
})
export class LogInComponent implements OnInit {
  isLoading = false;
  error: any;

  constructor(
    private loginService: LoginService,
    private snackBar: MatSnackBar,
    private router: Router) {
  }

  ngOnInit(): void {
  }

  onSubmit(form: NgForm) {
    console.log(form.value);
    if (!form.valid) {
      return;
    }
    // const username = 'admin';
    // const password = 'admin';
    const username = form.value.username;
    const password = form.value.password;

    this.isLoading = true;

    this.loginService.login(username, password).subscribe((tokenResponse: AuthResponse) => {
      sessionStorage.setItem('token', JSON.stringify(tokenResponse));
      this.isLoading = false;
      this.router.navigate(['/tenant'])
    }, errorMessage => {
      this.error = errorMessage;
      this.snackBar.open(errorMessage, '', {
        panelClass: ['mat-toolbar', 'mat-warn'], // mat-accent mat-primary
        duration: 5000
      });
      this.isLoading = false;
    });
    form.reset();
  }

}
