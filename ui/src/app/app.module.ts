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

const routes: Routes = [
  {
    path: "home",
    component: HomeComponent,
    data: { technology: "prout" }
  },
  {
    path: "subMenu/:title/:id",
    component: SubMenuComponent,
    data: { technology: "prout" }
  },
  {
    path: "page/:title/:id",
    component: OeuvreComponent,
    data: { technology: "prout" }
  },

  {
    path: "**",
    redirectTo: "/home",
    pathMatch: "full"
  }
];

@NgModule({

   imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    HttpClientXsrfModule.withOptions({
      cookieName: 'Csrf-Token',
      headerName: 'Csrf-Token',
    }),

    RouterModule.forRoot(routes)
  ],
  declarations: [
    AppComponent,
    RouteExampleComponent,
    MainMenuComponent,
    SubMenuComponent,
    ImageUploadComponent,
    MessageComponent,
    HomeComponent,
    OeuvreComponent
 ],
  providers: [
    AppService,
    {
      multi: true,
      provide: HTTP_INTERCEPTORS,
      useClass: AppHttpInterceptorService
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
