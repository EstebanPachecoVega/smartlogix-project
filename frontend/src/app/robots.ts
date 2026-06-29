import type { MetadataRoute } from 'next'

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: '*',
      allow: '/',
      disallow: ['/logistica/', '/dashboard/', '/api/', '/login', '/register'],
    },
    sitemap: 'https://smartlogix.cl/sitemap.xml',
  }
}
