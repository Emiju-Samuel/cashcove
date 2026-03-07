const FrequencyBadge = ({ frequency }) => {
  const frequencyStyles = {
    MONTHLY: { bg: "bg-blue-100", text: "text-blue-800", label: "Monthly" },
    QUARTERLY: { bg: "bg-purple-100", text: "text-purple-800", label: "Quarterly" },
    SEMI_ANNUAL: { bg: "bg-indigo-100", text: "text-indigo-800", label: "Semi-Annual" },
    YEARLY: { bg: "bg-violet-100", text: "text-violet-800", label: "Yearly" },
  };

  const style = frequencyStyles[frequency] || frequencyStyles.MONTHLY;

  return (
    <span className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${style.bg} ${style.text}`}>
      {style.label}
    </span>
  );
};

export default FrequencyBadge;
