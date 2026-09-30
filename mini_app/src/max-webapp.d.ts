export {}

declare global {
    interface Window {
        WebApp?: {
            initData: string
            initDataUnsafe: {
                start_param?: string
                user?: {
                    id: number
                }
            }
        }
    }
}
