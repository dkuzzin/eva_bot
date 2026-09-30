export function getMaxUserId(): number | null {
    return window.WebApp?.initDataUnsafe.user?.id ?? null
}

export function getMaxUserHeaders(): Record<string, string> {
    const userId = getMaxUserId()

    if (userId === null) {
        return {}
    }

    return {
        'X-Max-User-Id': String(userId),
    }
}