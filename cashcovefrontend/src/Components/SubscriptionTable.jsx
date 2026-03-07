import { useState, useMemo } from 'react';
import { ChevronUp, ChevronDown, Edit2, Trash2, Search } from 'lucide-react';
import StatusChip from './StatusChip';
import FrequencyBadge from './FrequencyBadge';
import RenewalCountdown from './RenewalCountdown';

const SubscriptionTable = ({ subscriptions, onEdit, onDelete, isLoading }) => {
  const [sortConfig, setSortConfig] = useState({ key: 'nextRenewalDate', direction: 'asc' });
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-NG', {
      style: 'currency',
      currency: 'NGN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  // Filter and search
  const filtered = useMemo(() => {
    return subscriptions.filter((sub) => {
      const matchesSearch =
        sub.subscriptionName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        sub.categoryName?.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesStatus =
        filterStatus === 'ALL' || sub.subscriptionStatus === filterStatus;

      return matchesSearch && matchesStatus;
    });
  }, [subscriptions, searchTerm, filterStatus]);

  // Sort
  const sorted = useMemo(() => {
    const items = [...filtered];
    items.sort((a, b) => {
      let aVal = a[sortConfig.key];
      let bVal = b[sortConfig.key];

      // Handle date sorting
      if (sortConfig.key === 'nextRenewalDate' || sortConfig.key === 'startDate') {
        aVal = new Date(aVal);
        bVal = new Date(bVal);
      }

      // Handle numeric sorting
      if (sortConfig.key === 'amount') {
        aVal = parseFloat(aVal);
        bVal = parseFloat(bVal);
      }

      if (aVal < bVal) return sortConfig.direction === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortConfig.direction === 'asc' ? 1 : -1;
      return 0;
    });

    return items;
  }, [filtered, sortConfig]);

  // Pagination
  const totalPages = Math.ceil(sorted.length / itemsPerPage);
  const startIdx = (currentPage - 1) * itemsPerPage;
  const paginatedData = sorted.slice(startIdx, startIdx + itemsPerPage);

  const handleSort = (key) => {
    setSortConfig((prev) => ({
      key,
      direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc',
    }));
  };

  const SortIcon = ({ columnKey }) => {
    if (sortConfig.key !== columnKey) return <div className="w-4 h-4" />;
    return sortConfig.direction === 'asc' ? <ChevronUp size={16} /> : <ChevronDown size={16} />;
  };

  const TableHeader = ({ label, sortKey, align = 'left' }) => (
    <th
      onClick={() => handleSort(sortKey)}
      className={`px-6 py-3 text-left text-xs font-semibold text-gray-700 bg-gray-50 border-b border-gray-200 cursor-pointer hover:bg-gray-100 transition text-${align}`}
    >
      <div className="flex items-center gap-1">
        {label}
        <SortIcon columnKey={sortKey} />
      </div>
    </th>
  );

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-blue-600" />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Search and Filter */}
      <div className="flex flex-col md:flex-row gap-4 items-stretch md:items-center">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-3 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="Search subscriptions..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(1);
            }}
            className="w-full pl-10 pr-4 py-2 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div className="flex gap-2">
          {['ALL', 'ACTIVE', 'PAUSED', 'CANCELLED'].map((status) => (
            <button
              key={status}
              onClick={() => {
                setFilterStatus(status);
                setCurrentPage(1);
              }}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
                filterStatus === status
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
              }`}
            >
              {status}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      {paginatedData.length > 0 ? (
        <div className="overflow-x-auto bg-white rounded-lg shadow">
          <table className="w-full">
            <thead>
              <tr>
                <TableHeader label="Subscription" sortKey="subscriptionName" />
                <TableHeader label="Amount" sortKey="amount" align="right" />
                <TableHeader label="Frequency" sortKey="frequency" />
                <TableHeader label="Next Renewal" sortKey="nextRenewalDate" />
                <TableHeader label="Status" sortKey="subscriptionStatus" />
                <th className="px-6 py-3 text-xs font-semibold text-gray-700 bg-gray-50 border-b border-gray-200">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {paginatedData.map((subscription) => (
                <tr key={subscription.id} className="border-b hover:bg-gray-50 transition last:border-b-0">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <span className="text-xl">{subscription.icon || '📦'}</span>
                      <div>
                        <p className="font-medium text-gray-900">{subscription.subscriptionName}</p>
                        <p className="text-xs text-gray-500">{subscription.categoryName}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-right font-semibold text-gray-900">
                    {formatCurrency(subscription.amount)}
                  </td>
                  <td className="px-6 py-4">
                    <FrequencyBadge frequency={subscription.frequency} />
                  </td>
                  <td className="px-6 py-4">
                    <RenewalCountdown nextRenewalDate={subscription.nextRenewalDate} />
                  </td>
                  <td className="px-6 py-4">
                    <StatusChip status={subscription.subscriptionStatus} />
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => onEdit(subscription)}
                        className="p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded transition"
                        title="Edit"
                      >
                        <Edit2 size={16} />
                      </button>
                      <button
                        onClick={() => {
                          if (window.confirm('Are you sure you want to delete this subscription?')) {
                            onDelete(subscription.id);
                          }
                        }}
                        className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded transition"
                        title="Delete"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="text-center py-8 bg-white rounded-lg">
          <p className="text-gray-600">No subscriptions match your search or filters.</p>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between bg-white rounded-lg p-4 shadow">
          <p className="text-sm text-gray-600">
            Showing {startIdx + 1} to {Math.min(startIdx + itemsPerPage, sorted.length)} of{' '}
            {sorted.length} results
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
              disabled={currentPage === 1}
              className="px-3 py-1 rounded border border-gray-300 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Previous
            </button>
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
              <button
                key={page}
                onClick={() => setCurrentPage(page)}
                className={`px-3 py-1 rounded text-sm font-medium transition ${
                  currentPage === page
                    ? 'bg-blue-600 text-white'
                    : 'border border-gray-300 hover:bg-gray-50'
                }`}
              >
                {page}
              </button>
            ))}
            <button
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
              disabled={currentPage === totalPages}
              className="px-3 py-1 rounded border border-gray-300 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default SubscriptionTable;
