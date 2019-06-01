import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { map, catchError } from 'rxjs/operators';
import { Observable, of } from 'rxjs/index';
import { MessageInternService } from './message-intern.service';

/**
 * Class representing application service.
 *
 * @class AppService.
 */
@Injectable()
export class AppService {
  private serviceUrl = '/api/summary';
  private dataPostTestUrl = '/api/postTest';
  private dataPostImage = '/api/image';

  constructor(private http: HttpClient,private ms : MessageInternService) {
  }

  /**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public getWelcomeMessage() {
    return this.http.get(this.serviceUrl).pipe(
      map(response => response)
    );
  }

  /**
   * Makes a http post request to send some data to backend & get response.
   */
  public sendData(data): Observable<any> {
    return this.http.post(this.dataPostTestUrl, data);
  }

   /**
     * Makes a http post request to send some data to backend & get response.
     */
    public sendImage(data): Observable<any> {
      return this.http.post(this.dataPostImage, data).pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.ms.push({ content: "Error with server, dev mode" });
          return of({link : "FAKE LINK"});
        })
      );
    }

     /**
     * Makes a http post request to send some data to backend & get response.
     */
    public sendImageTo(data,url): Observable<any> {
      return this.http.post(url, data).pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.ms.push({ content: "Error with server, dev mode" });
          return of({link : "FAKE LINK"});
        })
      );
    }
}
