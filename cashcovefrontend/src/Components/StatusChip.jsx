const StatusChip = ({ status }) => {
  const statusStyles = {
    ACTIVE: { bg: "bg-green-100", text: "text-green-800", label: "Active" },
    PAUSED: { bg: "bg-yellow-100", text: "text-yellow-800", label: "Paused" },
    CANCELLED: { bg: "bg-gray-100", text: "text-gray-800", label: "Cancelled" },
    EXPIRED: { bg: "bg-red-100", text: "text-red-800", label: "Expired" },
    PENDING_RENEWAL: { bg: "bg-blue-100", text: "text-blue-800", label: "Pending" },
  };

  const style = statusStyles[status] || statusStyles.ACTIVE;

  return (
    <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${style.bg} ${style.text}`}>
      {style.label}
    </span>
  );
};

export default StatusChip;
