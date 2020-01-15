import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Observable, of } from 'rxjs';

@Component({
  selector: 'app-page-navigation',
  templateUrl: './page-navigation.component.html',
  styleUrls: ['./page-navigation.component.css']
})
export class PageNavigationComponent implements OnInit {

  @Input()
  allElementsObs: Observable<any[]> = of([]);
  allElements: any[] = []
  @Output()
  viewElements: EventEmitter<any[]> = new EventEmitter<any[]>();
  private _offset = 10;
  start = 0;
  end = this._offset;

  constructor() {}

  get offset(): string {
    return this._offset.toString();
  }
  get currentPage(): number {
    return Math.ceil(this.start / this._offset) + 1;
  }
  currentPageNext(int: number): number {
    return this.currentPage + int;
  }
  set offset(off: string) {
    const n = parseInt(off, undefined);
    this._offset = n === NaN ? this._offset : n;
    this.end = this.start + this._offset;
    this.refreshView();
  }
  ngOnInit() {
    this.allElementsObs.subscribe((n)=> {
      this.allElements = n;
      this.refreshView();
    })
  }
  nbPage() {
    return Math.ceil(this.allElements.length / this._offset);
  }



  refreshView() {
    const start = this.allElements.length > this.start ? this.start : 0;
    const end =
      this.allElements.length >= this.end ? this.end : this.allElements.length;
    if (end - start > 0) {
      this.viewElements.emit(this.allElements.slice(start, end));
    } else {
      this.viewElements.emit([]);
    }
  }
  nextIsDisabled(nb: number) {
    return this.start + this._offset * nb < this.allElements.length;
  }
  nextStyle(nb: number): string {
    let sd: string;
    if (this.nextIsDisabled(nb)) {
      sd = "";
    } else {
      sd = "disabled";
    }
    return sd;
  }
  previousStyle(): string {
    if (this.start - this._offset >= 0) {
      return "";
    } else {
      return "disabled";
    }
  }
  previous() {
    this.start -= this._offset;
    this.end -= this._offset;
    this.refreshView();
  }
  next(nb: number): void {
    if (nb) {
      for (let i = 0; i < nb; i++) {
        this.start += this._offset;
        this.end += this._offset;
      }
    } else {
      this.start += this._offset;
      this.end += this._offset;
    }

    this.refreshView();
  }

}

