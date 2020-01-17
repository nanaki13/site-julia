import { Injectable, EventEmitter } from "@angular/core";
import { of, Observable } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { HttpClient } from "@angular/common/http";
import { MessageInternService } from "./message-intern.service";
import { environment } from "src/environments/environment";
import { Image } from "./Image";

@Injectable({
  providedIn: "root"
})
export class ImageService {
  private _images: Observable<string[]> = of([]);
  private _lastSelectedImage: EventEmitter<Image> = new EventEmitter<Image>();
  constructor(private http: HttpClient, private ms: MessageInternService) {}

  /**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public images(): Observable<Image[]> {
    return this.http.get<Image[]>(environment.imageUrl).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server" });
        return of([]);
      })
    );
  }

  imageSelected(img: Image) {
    if (history.state) {
      console.log(history);
      history.state.image =img;
    }
    this._lastSelectedImage.emit(img);
  }

  get lastSelectedImage(): Observable<Image> {
    return this._lastSelectedImage;
  }

  delete(id: number): Observable<boolean> {
    return this.http.delete<any>(environment.imageUrl + "/" + id).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server, see log" });
        return of(false);
      }),

      map(e => {
        if (e === false) {
          return e;
        } else {
          return true;
        }
      })
    );
  }

  sendImageTo(data): Observable<Image> {
    return this.http.post<Image>(environment.imageUrl, data).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server, dev mode" });
        return of({ link: "FAKE LINK" } as Image);
      })
    );
  }
  update(img: Image): Observable<boolean> {
    return this.http.patch<any>(environment.imageUrl, img).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server, see log" });
        return of(false);
      }),
      map(e => {
        if (e === false) {
          return e;
        } else {
          return true;
        }
      })
    );
  }
}
