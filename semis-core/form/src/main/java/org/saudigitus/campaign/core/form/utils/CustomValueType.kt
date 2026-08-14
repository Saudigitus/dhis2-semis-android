package org.saudigitus.campaign.core.form.utils

/**
 * Defines custom value types supported by the form engine.
 */
enum class CustomValueType {

    /**
     * Numeric field that allows users to increase or decrease a value
     * using increment/decrement controls instead of manual text input.
     *
     * Example:
     * - Number of students present
     * - Inventory quantity
     * - Household member count
     */
    COUNTER,

    /**
     * Organizational Unit selector with search capabilities.
     *
     * Intended for scenarios where the number of organizational units
     * is large and users need to quickly find a specific unit.
     *
     * Example:
     * - School selection
     * - Health facility selection
     * - District selection
     */
    SEARCHABLE_ORG_UNIT_FIELD,

    /**
     * Generic searchable field that allows users to search and select
     * a value from a predefined list of options.
     *
     * Example:
     * - Student selection
     * - Teacher selection
     * - Product selection
     */
    SEARCHABLE_FIELD,

    /**
     * Standard Organizational Unit selector.
     *
     * Unlike SEARCHABLE_ORG_UNIT_FIELD, this type is generally used
     * when the available organizational units can be presented through
     * a hierarchy, tree structure, or simple dropdown.
     *
     * Example:
     * - Province
     * - District
     * - School
     */
    ORG_UNIT,

    /**
     * Field whose value is obtained by scanning a QR code.
     *
     * The scanned content is automatically assigned to the field value.
     *
     * Example:
     * - Student ID
     * - Asset code
     * - Registration identifier
     */
    QR_FIELD,

    /**
     * Dropdown field that supports both manual selection and QR/Barcode
     * scanning to automatically locate and select an option.
     *
     * Example:
     * - Product lookup
     * - Student lookup
     * - Asset selection
     */
    SCANNABLE_DROPDOWN_FIELD,

    DROPDOWN
}