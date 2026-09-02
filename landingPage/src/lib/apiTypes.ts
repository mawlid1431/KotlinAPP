/**
 * API response types.
 *
 * Timestamps arrive as epoch milliseconds and money as plain numbers — the API
 * normalises Prisma's Date and Decimal at the edge, so these stay simple.
 */

export type AdminRole = 'superadmin' | 'staff';

export type AdminAccount = {
  id: string;
  username: string;
  displayName: string;
  email: string;
  role: AdminRole;
  isSuperAdmin: boolean;
};

export type StaffMember = {
  id: string;
  username: string;
  displayName: string;
  email: string;
  role: AdminRole;
  active: boolean;
  createdAt: number;
};

export type LoginResponse = {
  token: string;
  expiresAt: number;
  admin: Omit<AdminAccount, 'isSuperAdmin'>;
};

export type Branch = {
  id: string;
  slug: string;
  label: string;
  address: string;
  hours: string;
  imageUrl: string | null;
  imagePublicId: string | null;
  lat: number;
  lng: number;
  active: boolean;
  sortOrder: number;
  createdAt: number;
  updatedAt: number;
};

export type MenuItem = {
  id: string;
  legacyId?: number;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl: string;
  imagePublicId?: string;
  rating?: number;
  calories?: number;
  badge?: string;
  active: boolean;
  sortOrder: number;
  createdAt: number;
  updatedAt: number;
};

export type Promo = {
  id: string;
  code: string;
  title: string;
  subtitle: string;
  imageUrl: string | null;
  imagePublicId: string | null;
  discountPercent: number | null;
  fixedOff: number | null;
  minSpend: number | null;
  active: boolean;
  sortOrder: number;
  createdAt: number;
  updatedAt: number;
};

export type OrderStatus = 'active' | 'delivered' | 'cancelled';
export type OrderType = 'delivery' | 'pickup';
export type PayMethod = 'tng' | 'card' | 'banking';

export type OrderLine = {
  menuItemId?: number;
  name: string;
  price: number;
  qty: number;
  sugar?: string;
  ice?: string;
};

export type Order = {
  id: string;
  orderNumber: string;
  userId?: string;
  branchSlug?: string;
  branchLabel: string;
  orderType: OrderType;
  payMethod: PayMethod;
  status: OrderStatus;
  trackingStep: number;
  items: OrderLine[];
  subtotal: number;
  discount: number;
  deliveryFee: number;
  total: number;
  promoCode?: string;
  pointsEarned: number;
  pointsRedeemed: number;
  orderNote?: string;
  createdAt: number;
  updatedAt: number;
};

export type Customer = {
  id: string;
  name: string;
  email: string;
  pictureUrl?: string;
  branchSlug?: string;
  points: number;
  suspended: boolean;
  createdAt: number;
};

export type DashboardOverview = {
  computedAt: number;
  periodDays: number;
  totalOrders: number;
  activeOrders: number;
  deliveredOrders: number;
  totalRevenue: number;
  periodRevenue: number;
  totalCustomers: number;
  newCustomers: number;
  menuItems: number;
  activePromos: number;
  branches: number;
  ordersTrend: { timestamp: number; orders: number; revenue: number }[];
  ordersByBranch: { slug: string; label: string; orders: number; revenue: number }[];
  ordersByStatus: { status: string; count: number }[];
};
