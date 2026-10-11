package com.pes.studentorganizer.model

import com.pes.studentorganizer.R

// Sample data models - no Room database or network calls (out of scope).

data class Student(
    val name: String,
    val studentId: String,
    val major: String,
    val avatarRes: Int,
    val gpa: Double,
    val completedCredits: Int,
    val currentSemester: String
)

data class Course(
    val courseName: String,
    val courseCode: String,
    val instructor: String,
    val progress: Float // 0.0f to 1.0f, shown in a progress bar
)

data class Assignment(
    val id: Int,
    val title: String,
    val course: String,
    val dueDate: String, // stored as yyyy-MM-dd so sorting the text also sorts by date
    val priority: String, // "Low", "Medium" or "High"
    val description: String = "",
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = false
)

val sampleStudent = Student(
    name = "Pramiti Udupa",
    studentId = "PES1UG24CS331",
    major = "Computer Science & Engineering",
    avatarRes = R.drawable.ic_student_avatar,
    gpa = 3.82,
    completedCredits = 84,
    currentSemester = "Fall 2026"
)

val sampleCourses = listOf(
    Course("Data Structures", "CS101", "Prof. Ramesh Kumar", 0.72f),
    Course("Linear Algebra", "MATH201", "Prof. Anita Rao", 0.55f),
    Course("Technical Writing", "ENG102", "Prof. David Thomas", 0.40f),
    Course("Android App Development", "CS343", "Prof. Meera Iyer", 0.65f)
)

// 7 pending + 2 completed assignments
val sampleAssignments = listOf(
    Assignment(1, "Linked List Implementation", "CS101", "2026-10-12", "High",
        "Implement a singly linked list with insert, delete and search."),
    Assignment(2, "Matrix Operations Worksheet", "MATH201", "2026-10-14", "Medium",
        "Problems 1-20 from Chapter 3."),
    Assignment(3, "Compose UI Lab", "CS343", "2026-10-13", "High",
        "Build a screen using Column, Row and Box.", reminderEnabled = true),
    Assignment(4, "Research Essay Draft", "ENG102", "2026-10-20", "Low",
        "First draft of the 1500-word research essay."),
    Assignment(5, "Binary Search Tree Quiz", "CS101", "2026-10-16", "Medium"),
    Assignment(6, "Eigenvalues Problem Set", "MATH201", "2026-10-22", "High"),
    Assignment(7, "Navigation Mini Project", "CS343", "2026-10-25", "Medium",
        "Add NavHost with three destinations."),
    Assignment(8, "Stack and Queue Lab", "CS101", "2026-10-05", "Low", isCompleted = true),
    Assignment(9, "Resume Writing Exercise", "ENG102", "2026-10-03", "Low", isCompleted = true)
)
