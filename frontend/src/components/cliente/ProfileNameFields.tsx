import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { censorEmail } from '@/lib/emailUtils';

interface ProfileNameFieldsProps {
    primerNombre: string;
    segundoNombre: string;
    primerApellido: string;
    segundoApellido: string;
    email: string;
    disabled: boolean;
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export default function ProfileNameFields({
    primerNombre,
    segundoNombre,
    primerApellido,
    segundoApellido,
    email,
    disabled,
    onChange,
}: ProfileNameFieldsProps) {
    return (
        <>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                    <Label>Primer nombre *</Label>
                    <Input name="primerNombre" value={primerNombre} onChange={onChange} required disabled={disabled} placeholder="Ingresar primer nombre" />
                </div>
                <div>
                    <Label>Segundo nombre (opcional)</Label>
                    <Input name="segundoNombre" value={segundoNombre} onChange={onChange} disabled={disabled} placeholder="Ingresar segundo nombre (opcional)" />
                </div>
                <div>
                    <Label>Primer apellido *</Label>
                    <Input name="primerApellido" value={primerApellido} onChange={onChange} required disabled={disabled} placeholder="Ingresar primer apellido" />
                </div>
                <div>
                    <Label>Segundo apellido (opcional)</Label>
                    <Input name="segundoApellido" value={segundoApellido} onChange={onChange} disabled={disabled} placeholder="Ingresar segundo apellido (opcional)" />
                </div>
            </div>
            <div>
                <Label>Correo electrónico</Label>
                <p className="text-sm text-muted-foreground mt-1">{censorEmail(email)}</p>
                <p className="text-xs text-muted-foreground/70">El correo no puede ser modificado directamente.</p>
            </div>
        </>
    );
}
