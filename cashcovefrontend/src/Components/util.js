export const addThousandsSeperator = (num) => {
    if(num == null || isNaN(num)) return "";

    // Convert to number to ensure proper handling
    const numValue = Number(num);
    
    // Use toLocaleString for international formatting (e.g., 1,234,567.89 or 1.234.567,89)
    // Using 'en-US' format for standard international format with comma as thousand separator
    return numValue.toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}