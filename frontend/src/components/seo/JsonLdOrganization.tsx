const BASE_URL = 'https://smartlogix.cl';

const jsonLd = {
  '@context': 'https://schema.org',
  '@type': 'Organization',
  name: 'SmartLogix',
  url: BASE_URL,
  logo: `${BASE_URL}/logo.png`,
  sameAs: [
    'https://instagram.com/smartlogix',
    'https://facebook.com/smartlogix',
    'https://wa.me/56912345678',
  ],
  contactPoint: {
    '@type': 'ContactPoint',
    telephone: '+56 9 1234 5678',
    contactType: 'customer service',
    availableLanguage: ['Spanish'],
  },
};

export default function JsonLdOrganization() {
  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
    />
  );
}
