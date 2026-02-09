export interface AttendeeSignature {
  participantId: number;
  fullName: string;
  signatureData?: string;
}

export interface StudentEngagement {
  participantId: number;
  fullName: string;
  email: string;
  status: string;             // PRESENT, ABSENT, LATE, EXCUSED
  notes?: string;
  attendancePercent?: number;
  signatureData?: string;
  signatureType?: string;     // PRESENT or ACKNOWLEDGMENT
}

export interface SingleSessionReport {
  sessionId: number;
  programTitle: string;
  sessionTopic: string;
  startDateTime: string;
  instructorName: string;
  sessionType: string;
  notes?: string;
  contentCoverage: string[];
  studentEngagement: StudentEngagement[];
  attendedStudentSignatures: AttendeeSignature[];
  presentCount: number;
  absentCount: number;
  lateCount: number;
  excusedCount: number;
  totalEnrolled: number;
}

export interface SessionAttendanceReport {
  sessionId: number;
  programId: number;
  programTitle: string;
  sessionTopic: string;
  startDateTime: string;
  attendedStudentNames: string[];
  attendedStudentSignatures: AttendeeSignature[];
  instructorName: string;
  sessionType: string;
  notes?: string;
}

export interface StudentParticipation {
  participantId: number;
  fullName: string;
  email: string;
  sessionsAttended: number;
  totalSessions: number;
  attendancePercent: number;
  attendedSessionTopics: string[];
  participationTrend: string;
}

export interface DateBasedGroupedReport {
  fromDate: string;
  toDate: string;
  sessionsByDate: SessionAttendanceReport[];
  byStudent: StudentParticipation[];
  totalSessions: number;
  totalStudents: number;
  overallParticipationRate: number;
}

