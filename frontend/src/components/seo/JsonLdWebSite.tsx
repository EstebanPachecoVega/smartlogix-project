const BASE_URL = 'https://smartlogix.cl';

const jsonLd = {
  '@context': 'https://schema.org',
  '@type': 'WebSite',
  name: 'SmartLogix',
  url: BASE_URL,
  potentialAction: {
    '@type': 'SearchAction',
    target: {
      '@type': 'EntryPoint',
      urlTemplate: `${BASE_URL}/?search={search_term_string}`,
    },
    'query-input': 'required name=search_term_string',
  },
};

export default function JsonLdWebSite() {
  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
    />
  );
}
