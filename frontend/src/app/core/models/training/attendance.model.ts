export interface Attendance {
  id: number;
  sessionId: number;
  sessionTitle?: string;
  participantId: number;
  participantUsername?: string;
  participantFullName?: string;
  participantEmail?: string;
  status: string; // PRESENT, ABSENT, LATE, EXCUSED
  attendanceDate?: string; // ISO datetime string
  notes?: string;
  markedById: number;
  markedByUsername?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MarkAttendanceRequest {
  sessionId: number;
  attendances: StudentAttendance[];
}

export interface StudentAttendance {
  participantId: number;
  status: string; // PRESENT, ABSENT, LATE, EXCUSED
  notes?: string;
  attendanceDate?: string; // ISO datetime string
}

export enum AttendanceStatus {
  PRESENT = 'PRESENT',
  ABSENT = 'ABSENT',
  LATE = 'LATE',
  EXCUSED = 'EXCUSED'
}
