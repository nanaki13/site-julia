import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Updatable } from '../Updatable';

@Component({
  selector: 'app-btn-update',
  templateUrl: './btn-update.component.html',
  styleUrls: ['./btn-update.component.css']
})
export class BtnUpdateComponent implements OnInit {

  @Input()
  element : Updatable;
  @Output()
  elementOut =new EventEmitter();
  @Input()
  elementName : string;
  constructor() { }

  ngOnInit() {
  }
  update(el : Updatable){
    this.elementOut.emit(el)
  }
}
