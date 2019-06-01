import { Component, OnInit, Input } from '@angular/core';
import { AppService } from '../app.service';

@Component({
  selector: 'app-image-upload',
  templateUrl: './image-upload.component.html',
  styleUrls: ['./image-upload.component.css']
})
export class ImageUploadComponent implements OnInit {

  private postRequestResponse: string;
  private imgs: any[] = [];
  @Input()
  url: string;
  event(f) {
    console.log(f);
  }

  fileData = null;
  constructor(private appService: AppService) { }

  ngOnInit() {
  }


  handleFileInput(fileInput: any) {
    this.fileData = fileInput[0];
  }

  onSubmit() {
    const formData = new FormData();

    formData.append("file", this.fileData);
    this.appService.sendImageTo(formData,this.url).subscribe((data: any) => {
      this.postRequestResponse = data.link;
      this.imgs.push(data);
    });
  }
}
