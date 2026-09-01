package com.aiu.tdminsight.data.validation

// Distinguishes a hard blocking error from a soft "please review" warning.
// The UI uses this to colour messages: red for Error, amber for Warning.
sealed class FieldResult {
    object Valid : FieldResult()
    data class Error(val field: String, val message: String) : FieldResult()   // blocks calculation
    data class Warning(val field: String, val message: String) : FieldResult() // advisory only
}

data class ValidationReport(
    val errors: List<FieldResult.Error>   = emptyList(),
    val warnings: List<FieldResult.Warning> = emptyList(),
) {
    val isValid: Boolean get() = errors.isEmpty()
}
