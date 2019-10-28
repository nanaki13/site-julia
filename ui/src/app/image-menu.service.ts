import { Injectable } from "@angular/core";
import { of, Observable } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { HttpClient } from "@angular/common/http";
import { MessageInternService } from "./message-intern.service";

@Injectable({
  providedIn: "root"
})
export class ImageMenuService {

  private imgUrl = "/api/menu/images";
private _images: Observable<string[]>= of([])
constructor(private http: HttpClient,private ms: MessageInternService) { }


/**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public images(): Observable<string[]>  {
    return this.http.get<any[]>(this.imgUrl).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server" });
        return of([]);
      })
    ,map(e => e.map(e => e.link)));
  }
}




