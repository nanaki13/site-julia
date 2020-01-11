import { Component, OnInit } from '@angular/core';
import { ImageMenuService } from '../image-menu.service';
import * as $ from "jquery";
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {


  inter = new Map()
  //images: string[] = ["assets/img/mamie_muguette_acc.jpg"]
  images: string[] = []
  imgDom: any[] = []
  private _whantAddImage: boolean;

  constructor(private ims : ImageMenuService) { }

  addEventWhenisHere(id){
    this.inter.set(id,window.setInterval(() => {
       const doc = $("#"+id);
       console.log(doc)
       console.log(this.inter)
          if(doc != null){
            this.imgDom.push(doc)
            window.clearInterval(this.inter.get(id))
            this.inter.delete(id)
            if(this.inter.size ===0){
              swap(this.imgDom,0);


            }
          }
      },2000));
  }
  ngOnInit() {
    this.ims.images().subscribe((e) => {
      this.images = e;
      for(let i = 0; i  < this.images.length ;i++){
          this.addEventWhenisHere( i+"img");
      }
    });
  }




  addImageHome() {
    this._whantAddImage = true;
  }
  get whantAddImage(){
    return  this._whantAddImage;
  }
}
function swap(imgs,ind){
    imgs[ind].fadeOut(1000 );
    imgs[(ind + 1) % imgs.length  ].fadeIn(1000 );
    ind++;
    if(ind == imgs.length){
      ind = 0;
    }
    window.setTimeout(swap,4000,imgs,ind);
  }
