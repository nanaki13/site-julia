import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { PageElement } from '../model/PageElement';


@Component({
  selector: 'app-create-form',
  templateUrl: './create-form.component.html',
  styleUrls: ['./create-form.component.css']
})
export class CreateFormComponent implements OnInit {
  _showAdd: boolean =true;
  addIncolumn = 1;
  @Input()
  newItem : PageElement;

  @Input()
  typeList: string[] ;
  @Output()
  create = new EventEmitter<PageElement>();
  constructor() { }

  ngOnInit() {
  }

  showAdd(n : boolean) { this._showAdd = n;}

  createEntity(){
    this.newItem.x = this.addIncolumn - 1;
    this.create.emit(this.newItem);
  }

}
