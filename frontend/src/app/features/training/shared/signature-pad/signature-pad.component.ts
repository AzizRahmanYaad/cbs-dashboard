import { Component, ElementRef, ViewChild, AfterViewInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-signature-pad',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './signature-pad.component.html',
  styleUrls: ['./signature-pad.component.scss']
})
export class SignaturePadComponent implements AfterViewInit {
  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  @Output() signatureChange = new EventEmitter<string | null>();

  private ctx: CanvasRenderingContext2D | null = null;
  private isDrawing = false;
  private lastX = 0;
  private lastY = 0;

  ngAfterViewInit(): void {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas) return;
    this.ctx = canvas.getContext('2d');
    if (!this.ctx) return;

    const rect = canvas.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    this.ctx.scale(dpr, dpr);
    this.ctx.strokeStyle = '#000';
    this.ctx.lineWidth = 2;
    this.ctx.lineCap = 'round';
    this.ctx.lineJoin = 'round';
  }

  startDrawing(e: MouseEvent | TouchEvent): void {
    this.isDrawing = true;
    const pos = this.getPos(e);
    this.lastX = pos.x;
    this.lastY = pos.y;
  }

  draw(e: MouseEvent | TouchEvent): void {
    if (!this.isDrawing || !this.ctx) return;
    e.preventDefault();
    const pos = this.getPos(e);
    this.ctx.beginPath();
    this.ctx.moveTo(this.lastX, this.lastY);
    this.ctx.lineTo(pos.x, pos.y);
    this.ctx.stroke();
    this.lastX = pos.x;
    this.lastY = pos.y;
  }

  stopDrawing(): void {
    this.isDrawing = false;
  }

  private getPos(e: MouseEvent | TouchEvent): { x: number; y: number } {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas) return { x: 0, y: 0 };
    const rect = canvas.getBoundingClientRect();
    if (e instanceof TouchEvent) {
      const touch = e.touches[0] || e.changedTouches[0];
      return {
        x: touch.clientX - rect.left,
        y: touch.clientY - rect.top
      };
    }
    return {
      x: e.clientX - rect.left,
      y: e.clientY - rect.top
    };
  }

  clear(): void {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas || !this.ctx) return;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);
    this.signatureChange.emit(null);
  }

  getSignatureData(): string | null {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas) return null;
    const data = canvas.toDataURL('image/png');
    return data || null;
  }

  isEmpty(): boolean {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas || !this.ctx) return true;
    const imageData = this.ctx.getImageData(0, 0, canvas.width, canvas.height);
    const data = imageData.data;
    for (let i = 3; i < data.length; i += 4) {
      if (data[i] > 0) return false;
    }
    return true;
  }
}
