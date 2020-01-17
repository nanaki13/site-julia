import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { SourceService } from '../source.service';

@Component({
  selector: 'app-image-select',
  templateUrl: './image-select.component.html',
  styleUrls: ['./image-select.component.css']
})
export class ImageSelectComponent implements OnInit {

  @Input()
  selectable : any;

  @Output()
  selected : EventEmitter<any> = new EventEmitter<any>();
  @Output()
  unSelected : EventEmitter<any> = new EventEmitter<any>();

  checkChange(state : boolean){
    if(state){
      this.selected.emit(this.selectable);
    }else{
      this.unSelected.emit(this.selectable);
    }
  }
  constructor() { }

  ngOnInit() {
  }

}
