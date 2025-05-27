export interface StudentCourseDTO {
    id?: number;
    studentId?: number;
    studentName?: string;
    courseId: number;
    courseName: string;
    courseCode: string;
    teacherName: string;
    classTime: string;
    classroom: string;
    weeks: string;
    sections: string;
    selectionTime?: string;
    score?: number;
    attendance?: number;
    status?: string;
} 