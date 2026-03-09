// Helper function to prepare income data for the chart
export const prepareIncomeLineChartData = (transactions) => {
    if (!transactions || transactions.length === 0) return [];

    return transactions
        .map(t => ({
            date: t.date,
            amount: t.amount
        }))
        .sort((a, b) => {
            const [yearA, monthA, dayA] = a.date.split('-').map(Number);
            const dateA = new Date(yearA, monthA - 1, dayA);
            const [yearB, monthB, dayB] = b.date.split('-').map(Number);
            const dateB = new Date(yearB, monthB - 1, dayB);
            return dateA - dateB;
        });
};

// Format date for display
export const formatDate = (dateStr) => {
    const [year, month, day] = dateStr.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

// Format amount
export const formatAmount = (amount) => `${Number(amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

// Generate smooth curve path using cubic Bezier curves
export const generateSmoothPath = (data, scaleX, scaleY) => {
    if (data.length === 0) return '';
    if (data.length === 1) {
        const x = scaleX(0);
        const y = scaleY(data[0].amount);
        return `M${x},${y}`;
    }

    if (data.length === 2) {
        // For 2 points, just draw a straight line
        const x1 = scaleX(0);
        const y1 = scaleY(data[0].amount);
        const x2 = scaleX(1);
        const y2 = scaleY(data[1].amount);
        return `M${x1},${y1}L${x2},${y2}`;
    }

    // Get all points
    const points = data.map((d, i) => ({
        x: scaleX(i),
        y: scaleY(d.amount)
    }));

    // Start the path
    let pathData = `M${points[0].x},${points[0].y}`;

    // Generate smooth curves using Catmull-Rom spline
    for (let i = 0; i < points.length - 1; i++) {
        const p0 = points[i - 1] || points[0];
        const p1 = points[i];
        const p2 = points[i + 1];
        const p3 = points[i + 2] || points[points.length - 1];

        // Calculate control points
        const tension = 0.2;
        const cp1x = p1.x + (p2.x - p0.x) * tension;
        const cp1y = p1.y + (p2.y - p0.y) * tension;
        const cp2x = p2.x - (p3.x - p1.x) * tension;
        const cp2y = p2.y - (p3.y - p1.y) * tension;

        pathData += `C${cp1x},${cp1y} ${cp2x},${cp2y} ${p2.x},${p2.y}`;
    }

    return pathData;
};
