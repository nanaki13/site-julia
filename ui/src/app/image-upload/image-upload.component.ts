import { Component, OnInit, Input, HostListener, Output, EventEmitter } from "@angular/core";
import { AppService } from "../app.service";
import { FormGroup, FormControl, FormBuilder, Validators } from "@angular/forms";
import {  ImageService } from '../image.service';
import { Image } from "../Image";

@Component({
  selector: "app-image-upload",
  templateUrl: "./image-upload.component.html",
  styleUrls: ["./image-upload.component.css"]
})
export class ImageUploadComponent implements OnInit {
  imageName = "";
  @Output() newImage = new EventEmitter<Image>();
  fileData = null;
  sending = false;
  fileForm = this.fb.group({
    fileName: ["", Validators.required],
    file: ["", Validators.required]
  });


  get file() {return this.fileForm.get("file")}
  get fileName() {return this.fileForm.get("fileName")}


  constructor(private imageService: ImageService, private fb: FormBuilder) {}

  ngOnInit() {}

  handleFileInput(fileInput: any) {
    this.fileData = fileInput[0];
  }

  @HostListener('change', ['$event.target.files']) emitFiles( event: FileList ) {
    if(event){
      this.fileData  = event && event.item(0);
    }
  }
  onSubmit() {
    const formData = new FormData();
    formData.append("file", this.fileData);
    formData.append("image_name", this.fileName.value);
    this.sending = true;
    this.imageService.sendImageTo(formData).subscribe((data) => {
        this.newImage.emit(data);
        this.sending = false;
      });

  }

  submitiSDisable(){
      return !this.fileForm.valid || this.sending ;
  }
}
