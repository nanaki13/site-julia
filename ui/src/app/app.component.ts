// tslint:disable-next-line: quotemark
import { Component } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { AppService } from "./app.service";

@Component({
// tslint:disable-next-line: quotemark
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.css"]
})
export class AppComponent {

  constructor(/*private appService: AppService, private http: HttpClient*/) {
    // this.appService.getWelcomeMessage().subscribe((data: any) => {
    //   this.title = data.content;
    // });
  }

  /**
   * This method is used to test the post request
   */
  public postData(): void {
   /* this.appService.sendData({}).subscribe((data: any) => {
      this.postRequestResponse = data.content;
    });*/
  }


  ngOnInit() {}



}
