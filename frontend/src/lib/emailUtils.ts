export function censorEmail(email: string): string {
    if (!email) return '';
    const [localPart, domain] = email.split('@');
    if (localPart.length <= 3) {
        return `${localPart.charAt(0)}***@${domain.charAt(0)}***.${domain.split('.').pop()}`;
    }
    const visibleStart = localPart.substring(0, 3);
    const domainStart = domain.charAt(0);
    const domainEnd = domain.split('.').pop();
    return `${visibleStart}***@${domainStart}***.${domainEnd}`;
}