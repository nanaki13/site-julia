import { BrowserModule } from "@angular/platform-browser";
import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import {
  HTTP_INTERCEPTORS,
  HttpClientModule,
  HttpClientXsrfModule
} from "@angular/common/http";
import { FormsModule } from "@angular/forms";
import { AppComponent } from "./app.component";
import { RouteExampleComponent } from "./route-example/route-example.component";

import { AppService } from "./app.service";
import { AppHttpInterceptorService } from "./http-interceptor.service";
import { MainMenuComponent } from "./main-menu/main-menu.component";
import { SubMenuComponent } from "./sub-menu/sub-menu.component";
import { ImageUploadComponent } from "./image-upload/image-upload.component";
import { MessageComponent } from "./message/message.component";
import { HomeComponent } from "./home/home.component";
import { OeuvreComponent } from "./oeuvre/oeuvre.component";
import { OeuvreGalComponent } from "./oeuvre-gal/oeuvre-gal.component";
import { PageElementTableComponent } from "./page-element-table/page-element-table.component";
import { ImageViewComponent } from "./image-view/image-view.component";
import { ReactiveFormsModule } from '@angular/forms';
import { PageNavigationComponent } from './page-navigation/page-navigation.component';
import { ImageSelectComponent } from './image-select/image-select.component';
import { BtnUpdateComponent } from './btn-update/btn-update.component';
import { DisplayModeCommandComponent } from './display-mode-command/display-mode-command.component';
import { CreateFormComponent } from './create-form/create-form.component';

const routes: Routes = [
  {
    path: "galery/:title/:id",
    component: OeuvreGalComponent
  },
  {
    path: "home",
    component: HomeComponent
  },
  {
    path: "subMenu/:title/:id",
    component: SubMenuComponent
  },
  {
    path: "page/:title/:id",
    component: OeuvreComponent
  },
  {
    path: "admin/image",
    component: ImageViewComponent
  },
  {
    path: "**",
    redirectTo: "/home",
    pathMatch: "full"
  }
];

@NgModule({
   declarations: [
      AppComponent,
      RouteExampleComponent,
      PageElementTableComponent,
      MainMenuComponent,
      SubMenuComponent,
      ImageUploadComponent,
      MessageComponent,
      HomeComponent,
      OeuvreComponent,
      OeuvreGalComponent,
      ImageViewComponent,
      PageNavigationComponent,
      ImageSelectComponent,
      BtnUpdateComponent,
      DisplayModeCommandComponent,
      CreateFormComponent
   ],
   providers: [
    AppService,
    {
      multi: true,
      provide: HTTP_INTERCEPTORS,
      useClass: AppHttpInterceptorService
    }
   ],
   bootstrap: [
      AppComponent
   ],
   imports: [
      BrowserModule,
      FormsModule,
      ReactiveFormsModule,
      HttpClientModule,
      HttpClientXsrfModule.withOptions({
        cookieName: "Csrf-Token",
        headerName: "Csrf-Token"
      }),

      RouterModule.forRoot(routes)
    ]
})
export class AppModule {}
