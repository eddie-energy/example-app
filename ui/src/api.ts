import { keycloak } from './keycloak'
import type { Permission, PermissionDetailsType } from './types'

export const BASE_URL = THYMELEAF_BACKEND_BASE_URL ?? import.meta.env.VITE_BACKEND_BASE_URL

const FALLBACK_ERROR_MESSAGES = {
  400: 'This action was declined as invalid. Please check your input.',
  401: 'Authorization failed. Please log in again.',
  403: 'Access was denied. The action might not be available anymore. Please try and reload this page.',
  404: 'The target resource was not found. It might have been removed. Please try and reload this page.',
  500: 'Something went wrong. Please try again.',
}

async function fetch(path: string, init?: RequestInit): Promise<any> {
  try {
    await keycloak.updateToken(5)
  } catch (error) {
    console.error('Failed to update authentication token. Please reload this page.')
    throw error
  }

  const response = await window
    .fetch(BASE_URL + path, {
      headers: {
        Authorization: `Bearer ${keycloak.token}`,
        'Content-Type': 'application/json',
      },
      ...init,
    })
    .catch((error: unknown) => {
      console.error('Network error. Please check your connection.')
      throw error
    })

  if (!response.ok) {
    const message =
      (await parseErrorResponse(response)) ??
      FALLBACK_ERROR_MESSAGES[response.status as keyof typeof FALLBACK_ERROR_MESSAGES] ??
      'An unexpected error occurred. Please try again.'
    throw new Error(message)
  }

  if (response.headers.get('content-type')?.includes('application/json')) {
    return response.json()
  }

  return response.text()
}

/**
 * Helper for parsing error messages from response objects.
 * TODO: Simplify by enforcing a single format for all error messages on the backend.
 */
async function parseErrorResponse(response: Response): Promise<string> {
  // Check if response is JSON
  if (!response.headers.get('content-type')?.includes('application/json')) {
    return response.text()
  }

  const json = await response.json()

  // EDDIE-style error messages
  if (json.errors && Array.isArray(json.errors) && json.errors.length > 0) {
    return json.errors.map(({ message }: { message: string }) => message).join(' ')
  }

  // AIIDA-style error messages
  if (json.message) {
    return json.message
  }

  // Fallback for other formats
  return JSON.stringify(json)
}
export async function getPermissions(): Promise<Permission[]> {
  return fetch('/permissions')
}

export async function getPermissionByID(id: number): Promise<PermissionDetailsType> {
  return fetch(`/permissions/${id}`)
}

export async function removePermissionByID(id: number) : Promise<boolean> {
  return fetch(`/permissions/remove/${id}`)
}

export async function renamePermissionByID(id: number, newName: string) : Promise<boolean> {
  return fetch(`/permissions/rename/${id}?newName=${newName}`)
}