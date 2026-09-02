import { clearAdminSession, getAdminSession } from '@/admin/auth';

/** Base URL of the NestJS API. Set VITE_API_URL in .env.local. */
export const API_URL = (import.meta.env.VITE_API_URL as string | undefined)?.trim() || '';

export function hasApi(): boolean {
  return API_URL.length > 0;
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

type RequestOptions = {
  method?: 'GET' | 'POST' | 'PATCH' | 'DELETE';
  body?: unknown;
  /** Send the admin session token. Default true — nearly every call is admin-scoped. */
  auth?: boolean;
  signal?: AbortSignal;
};

async function readError(response: Response): Promise<string> {
  try {
    const payload = (await response.json()) as { message?: string };
    if (payload?.message) return payload.message;
  } catch {
    // Fall through to the generic message.
  }
  return `Request failed (${response.status})`;
}

/**
 * Single entry point for every API call.
 *
 * The admin session token rides in the Authorization header and is attached
 * here, so no caller ever has to handle it.
 */
export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  if (!hasApi()) {
    throw new ApiError('API is not configured. Set VITE_API_URL.', 0);
  }

  const { method = 'GET', body, auth = true, signal } = options;
  const headers: Record<string, string> = {};

  if (auth) {
    const token = getAdminSession()?.token;
    if (token) headers.Authorization = `Bearer ${token}`;
  }
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  const response = await fetch(`${API_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
    signal,
  });

  // An expired or revoked session should log the dashboard out, not loop.
  if (response.status === 401 && auth) {
    clearAdminSession();
    throw new ApiError(await readError(response), 401);
  }

  if (!response.ok) {
    throw new ApiError(await readError(response), response.status);
  }

  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}

export type UploadedImage = {
  imageUrl: string;
  publicId: string;
  width: number;
  height: number;
  format: string;
  bytes: number;
};

/**
 * Uploads an image through the API to Cloudinary and returns its secure URL and
 * public id. The caller submits both with the entity form so the database
 * stores only the reference — never the file.
 */
export async function uploadImage(
  file: File,
  folder: 'menu' | 'promos' | 'branches',
): Promise<UploadedImage> {
  if (!hasApi()) {
    throw new ApiError('API is not configured. Set VITE_API_URL.', 0);
  }

  const form = new FormData();
  form.append('file', file);
  form.append('folder', folder);

  const token = getAdminSession()?.token;
  const response = await fetch(`${API_URL}/admin/uploads/image`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: form,
  });

  if (!response.ok) {
    throw new ApiError(await readError(response), response.status);
  }
  return (await response.json()) as UploadedImage;
}
