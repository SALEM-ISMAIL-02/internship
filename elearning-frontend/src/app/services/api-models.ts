/** SRP: shared domain models — no HTTP, no logic. */

export interface Course {
  id?: number;
  title: string;
  description?: string;
  category?: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'MANDATORY';
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  instructor?: { id: number };
}

export interface Material {
  id?: number;
  type: string;
  filePath: string;
  originalName?: string;
  course?: { id: number };
}

export interface Question {
  id?: number;
  text: string;
  options: string[];
  correctAnswer: number;
}

export interface Quiz {
  id?: number;
  course?: { id: number };
  aiGenerated?: boolean;
  published?: boolean;
  questions: Question[];
}

export interface LearnerProgress {
  id?: number;
  progress: number;
  completed: boolean;
  course?: Course;
}

export interface QuizResult {
  id?: number;
  score?: number;
  date: string;
  quiz?: Quiz;
  courseId?: number;
  answersJson?: string;
  approved: boolean;
}
