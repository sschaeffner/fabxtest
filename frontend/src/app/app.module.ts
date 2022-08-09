import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { NgxsModule } from "@ngxs/store";
import { NgxsStoragePluginModule } from "@ngxs/storage-plugin";
import { NgxsReduxDevtoolsPluginModule } from "@ngxs/devtools-plugin";

import { ButtonModule } from 'primeng/button';
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { ColorPickerModule } from "primeng/colorpicker";
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from "primeng/inputtext";
import { MessageModule } from 'primeng/message';
import { MessagesModule } from "primeng/messages";
import { PasswordModule } from "primeng/password";
import { SkeletonModule } from "primeng/skeleton";
import { SplitButtonModule } from "primeng/splitbutton";
import { StyleClassModule } from "primeng/styleclass";
import { TableModule } from "primeng/table";
import { TagModule } from "primeng/tag";

import { environment } from "../environments/environment";
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { AuthGuard } from "./state/auth-guard";
import { DeviceDetailsComponent } from './device-details/device-details.component';
import { DevicesComponent } from './devices/devices.component';
import { FabxState } from "./state/fabx-state";
import { LoginComponent } from './login/login.component';
import { NavbarComponent } from './navbar/navbar.component';
import { NgxsRouterPluginModule } from "@ngxs/router-plugin";
import { QualificationAddComponent } from './qualification-add/qualification-add.component';
import { QualificationDetailsComponent } from './qualification-details/qualification-details.component';
import { QualificationsComponent } from './qualifications/qualifications.component';
import { UserAddComponent } from './user-add/user-add.component';
import { UserDetailsComponent } from './user-details/user-details.component';
import { UsersComponent } from './users/users.component';
import { ToolsComponent } from './tools/tools.component';
import { ToolDetailsComponent } from './tool-details/tool-details.component';

@NgModule({
    declarations: [
        AppComponent,
        DeviceDetailsComponent,
        DevicesComponent,
        LoginComponent,
        NavbarComponent,
        QualificationAddComponent,
        QualificationDetailsComponent,
        QualificationsComponent,
        UserAddComponent,
        UserDetailsComponent,
        UsersComponent,
        ToolsComponent,
        ToolDetailsComponent,
    ],
    imports: [
        BrowserAnimationsModule,
        BrowserModule,
        HttpClientModule,
        ReactiveFormsModule,
        FormsModule,

        NgxsModule.forRoot([FabxState], {
            developmentMode: !environment.production
        }),
        NgxsStoragePluginModule.forRoot({
            key: ["fabx.auth", "fabx.loggedInUserId"]
        }),
        NgxsRouterPluginModule.forRoot(),
        NgxsReduxDevtoolsPluginModule.forRoot(),

        ButtonModule,
        CardModule,
        CheckboxModule,
        ColorPickerModule,
        InputNumberModule,
        InputTextModule,
        MessageModule,
        MessagesModule,
        PasswordModule,
        SkeletonModule,
        SplitButtonModule,
        StyleClassModule,
        TableModule,
        TagModule,

        AppRoutingModule,
    ],
    providers: [AuthGuard],
    bootstrap: [AppComponent]
})
export class AppModule {
}
