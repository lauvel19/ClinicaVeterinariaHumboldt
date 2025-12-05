// Landing page principal de la Clínica Veterinaria Humboldt
// Muestra información institucional, servicios y llamado a la acción para iniciar sesión

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Heart, 
  Stethoscope, 
  Syringe, 
  Calendar, 
  Clock, 
  Phone, 
  Mail, 
  MapPin,
  Star,
  Shield,
  Users,
  ChevronDown,
  Menu,
  X,
  PawPrint
} from 'lucide-react';

export const LandingPage = () => {
  const navigate = useNavigate();
  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 50);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const scrollToSection = (sectionId: string) => {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
    setIsMobileMenuOpen(false);
  };

  return (
    <div className="min-h-screen bg-white">
      {/* Navbar */}
      <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        isScrolled ? 'bg-white/95 backdrop-blur-md shadow-lg' : 'bg-transparent'
      }`}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-20">
            {/* Logo */}
            <div className="flex items-center gap-3">
              <img 
                src="/LogoClinicaVeterinaria.png" 
                alt="Logo Clínica Veterinaria Humboldt" 
                className="h-12 w-auto"
              />
              <div className="hidden sm:block">
                <h1 className={`text-xl font-bold transition-colors ${
                  isScrolled ? 'text-secondary' : 'text-white'
                }`}>
                  Veterinaria Humboldt
                </h1>
              </div>
            </div>

            {/* Desktop Menu */}
            <div className="hidden md:flex items-center gap-8">
              {['Inicio', 'Servicios', 'Nosotros', 'Contacto'].map((item) => (
                <button
                  key={item}
                  onClick={() => scrollToSection(item.toLowerCase())}
                  className={`font-medium transition-colors hover:text-primary ${
                    isScrolled ? 'text-gray-700' : 'text-white'
                  }`}
                >
                  {item}
                </button>
              ))}
              <button
                onClick={() => navigate('/login')}
                className="bg-primary hover:bg-primary-dark text-white px-6 py-2.5 rounded-full font-semibold transition-all duration-300 hover:shadow-lg hover:shadow-primary/30 hover:scale-105"
              >
                Iniciar Sesión
              </button>
            </div>

            {/* Mobile Menu Button */}
            <button
              className="md:hidden p-2"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            >
              {isMobileMenuOpen ? (
                <X className={isScrolled ? 'text-gray-700' : 'text-white'} size={24} />
              ) : (
                <Menu className={isScrolled ? 'text-gray-700' : 'text-white'} size={24} />
              )}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {isMobileMenuOpen && (
          <div className="md:hidden bg-white border-t shadow-lg">
            <div className="px-4 py-4 space-y-3">
              {['Inicio', 'Servicios', 'Nosotros', 'Contacto'].map((item) => (
                <button
                  key={item}
                  onClick={() => scrollToSection(item.toLowerCase())}
                  className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg"
                >
                  {item}
                </button>
              ))}
              <button
                onClick={() => navigate('/login')}
                className="w-full bg-primary text-white px-6 py-3 rounded-full font-semibold mt-4"
              >
                Iniciar Sesión
              </button>
            </div>
          </div>
        )}
      </nav>

      {/* Hero Section */}
      <section id="inicio" className="relative min-h-screen flex items-center justify-center overflow-hidden">
        {/* Background Gradient */}
        <div className="absolute inset-0 bg-gradient-to-br from-secondary via-secondary-light to-primary"></div>
        
        {/* Decorative Elements */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute -top-40 -right-40 w-96 h-96 bg-primary/20 rounded-full blur-3xl"></div>
          <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-primary-light/20 rounded-full blur-3xl"></div>
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-white/5 rounded-full"></div>
        </div>

        {/* Floating Paw Prints */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          {[...Array(6)].map((_, i) => (
            <PawPrint
              key={i}
              className="absolute text-white/10 animate-pulse"
              size={40 + i * 10}
              style={{
                top: `${15 + i * 15}%`,
                left: `${5 + i * 18}%`,
                animationDelay: `${i * 0.5}s`,
                transform: `rotate(${i * 30}deg)`
              }}
            />
          ))}
        </div>

        {/* Content */}
        <div className="relative z-10 text-center px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto">
          <div className="mb-8 flex justify-center">
            <img 
              src="/LogoClinicaVeterinaria.png" 
              alt="Logo" 
              className="h-32 sm:h-40 w-auto drop-shadow-2xl animate-fade-in"
            />
          </div>
          
          <h1 className="text-4xl sm:text-5xl lg:text-7xl font-bold text-white mb-6 leading-tight">
            Clínica Veterinaria
            <span className="block text-primary-light">Humboldt</span>
          </h1>
          
          <p className="text-xl sm:text-2xl text-white/90 mb-10 max-w-3xl mx-auto leading-relaxed">
            Cuidamos a tu mascota como parte de nuestra familia. 
            Más de 10 años brindando amor y atención profesional.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <button
              onClick={() => navigate('/login')}
              className="group relative bg-primary hover:bg-primary-light text-white px-10 py-4 rounded-full font-bold text-lg transition-all duration-300 hover:shadow-2xl hover:shadow-primary/40 hover:scale-105 flex items-center gap-3"
            >
              <span>Iniciar Sesión</span>
              <div className="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center group-hover:bg-white/30 transition-colors">
                <ChevronDown className="rotate-[-90deg]" size={18} />
              </div>
            </button>
            
            <button
              onClick={() => scrollToSection('servicios')}
              className="text-white border-2 border-white/50 hover:border-white hover:bg-white/10 px-8 py-4 rounded-full font-semibold text-lg transition-all duration-300"
            >
              Conocer Servicios
            </button>
          </div>
        </div>

        {/* Scroll Indicator */}
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce">
          <ChevronDown className="text-white/70" size={32} />
        </div>
      </section>

      {/* Services Section */}
      <section id="servicios" className="py-24 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <span className="text-primary font-semibold text-sm uppercase tracking-wider">Nuestros Servicios</span>
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-bold text-secondary mt-3 mb-4">
              Todo lo que tu mascota necesita
            </h2>
            <p className="text-gray-600 max-w-2xl mx-auto text-lg">
              Ofrecemos una amplia gama de servicios veterinarios con la más alta calidad y tecnología de punta.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {[
              {
                icon: Stethoscope,
                title: 'Consulta General',
                description: 'Evaluación completa del estado de salud de tu mascota con diagnóstico preciso y tratamiento personalizado.',
                color: 'bg-blue-500'
              },
              {
                icon: Syringe,
                title: 'Vacunación',
                description: 'Programa completo de vacunación para proteger a tu mascota de enfermedades. Seguimiento y recordatorios.',
                color: 'bg-green-500'
              },
              {
                icon: Heart,
                title: 'Cirugía',
                description: 'Procedimientos quirúrgicos con equipos de última generación y personal altamente capacitado.',
                color: 'bg-red-500'
              },
              {
                icon: Calendar,
                title: 'Citas Online',
                description: 'Agenda tu cita de forma fácil y rápida desde nuestra plataforma digital. Sin esperas.',
                color: 'bg-purple-500'
              },
              {
                icon: Shield,
                title: 'Desparasitación',
                description: 'Control integral de parásitos internos y externos para mantener a tu mascota saludable.',
                color: 'bg-orange-500'
              },
              {
                icon: Clock,
                title: 'Emergencias 24/7',
                description: 'Atención de urgencias las 24 horas del día, los 7 días de la semana. Siempre estamos para ti.',
                color: 'bg-primary'
              },
            ].map((service, index) => (
              <div 
                key={index}
                className="group bg-white rounded-3xl p-8 shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-2 border border-gray-100"
              >
                <div className={`${service.color} w-16 h-16 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300`}>
                  <service.icon className="text-white" size={28} />
                </div>
                <h3 className="text-xl font-bold text-secondary mb-3">{service.title}</h3>
                <p className="text-gray-600 leading-relaxed">{service.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="nosotros" className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-16 items-center">
            {/* Image/Visual Side */}
            <div className="relative">
              <div className="bg-gradient-to-br from-primary to-secondary rounded-3xl p-8 sm:p-12 text-white relative overflow-hidden">
                <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2"></div>
                <div className="absolute bottom-0 left-0 w-48 h-48 bg-white/10 rounded-full translate-y-1/2 -translate-x-1/2"></div>
                
                <div className="relative z-10">
                  <img 
                    src="/LogoClinicaVeterinaria.png" 
                    alt="Logo" 
                    className="h-24 w-auto mb-8"
                  />
                  <h3 className="text-2xl font-bold mb-4">Nuestra Misión</h3>
                  <p className="text-white/90 text-lg leading-relaxed mb-8">
                    Brindar atención veterinaria de excelencia con amor y dedicación, 
                    mejorando la calidad de vida de las mascotas y la tranquilidad de sus familias.
                  </p>
                  
                  <div className="grid grid-cols-2 gap-6">
                    <div className="bg-white/10 rounded-2xl p-4 backdrop-blur-sm">
                      <Users className="mb-2" size={24} />
                      <div className="font-bold">Equipo Experto</div>
                      <div className="text-sm text-white/80">Profesionales certificados</div>
                    </div>
                    <div className="bg-white/10 rounded-2xl p-4 backdrop-blur-sm">
                      <Star className="mb-2" size={24} />
                      <div className="font-bold">Calidad Premium</div>
                      <div className="text-sm text-white/80">Equipos de última generación</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Content Side */}
            <div>
              <span className="text-primary font-semibold text-sm uppercase tracking-wider">Sobre Nosotros</span>
              <h2 className="text-3xl sm:text-4xl lg:text-5xl font-bold text-secondary mt-3 mb-6">
                Más que una clínica, una familia
              </h2>
              <p className="text-gray-600 text-lg mb-6 leading-relaxed">
                En la Clínica Veterinaria Humboldt llevamos más de una década dedicados al cuidado 
                integral de las mascotas. Nuestro equipo de veterinarios especializados trabaja 
                con pasión y compromiso para ofrecer el mejor servicio.
              </p>
              <p className="text-gray-600 text-lg mb-8 leading-relaxed">
                Contamos con instalaciones modernas, equipamiento de vanguardia y un ambiente 
                diseñado para que tanto las mascotas como sus dueños se sientan cómodos y seguros.
              </p>

              <div className="space-y-4">
                {[
                  'Veterinarios con más de 10 años de experiencia',
                  'Instalaciones amplias y modernas',
                  'Laboratorio clínico propio',
                  'Atención personalizada y trato humano',
                ].map((item, index) => (
                  <div key={index} className="flex items-center gap-3">
                    <div className="w-6 h-6 bg-primary/10 rounded-full flex items-center justify-center flex-shrink-0">
                      <div className="w-2 h-2 bg-primary rounded-full"></div>
                    </div>
                    <span className="text-gray-700">{item}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials Section */}
      <section className="py-24 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <span className="text-primary font-semibold text-sm uppercase tracking-wider">Testimonios</span>
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-bold text-secondary mt-3 mb-4">
              Lo que dicen nuestros clientes
            </h2>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                name: 'María García',
                pet: 'Dueña de Max (Golden Retriever)',
                text: 'Excelente atención y profesionalismo. Mi perrito Max siempre recibe el mejor cuidado. Los recomiendo totalmente.',
                rating: 5
              },
              {
                name: 'Carlos Rodríguez',
                pet: 'Dueño de Luna (Gato Persa)',
                text: 'El equipo de Humboldt salvó a mi gatita Luna. Eternamente agradecido por su dedicación y amor por los animales.',
                rating: 5
              },
              {
                name: 'Ana Martínez',
                pet: 'Dueña de Rocky (Bulldog)',
                text: 'Las instalaciones son increíbles y el sistema de citas online es muy práctico. Rocky ama ir a sus controles.',
                rating: 5
              },
            ].map((testimonial, index) => (
              <div 
                key={index}
                className="bg-white rounded-3xl p-8 shadow-lg border border-gray-100"
              >
                <div className="flex gap-1 mb-4">
                  {[...Array(testimonial.rating)].map((_, i) => (
                    <Star key={i} className="text-yellow-400 fill-yellow-400" size={20} />
                  ))}
                </div>
                <p className="text-gray-600 mb-6 italic">"{testimonial.text}"</p>
                <div>
                  <div className="font-bold text-secondary">{testimonial.name}</div>
                  <div className="text-sm text-gray-500">{testimonial.pet}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 bg-gradient-to-r from-secondary to-primary relative overflow-hidden">
        <div className="absolute inset-0">
          <div className="absolute top-0 left-1/4 w-96 h-96 bg-white/5 rounded-full blur-3xl"></div>
          <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-white/5 rounded-full blur-3xl"></div>
        </div>
        
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
          <h2 className="text-3xl sm:text-4xl lg:text-5xl font-bold text-white mb-6">
            ¿Listo para cuidar a tu mascota?
          </h2>
          <p className="text-white/90 text-xl mb-10 max-w-2xl mx-auto">
            Agenda tu cita ahora y descubre por qué miles de familias confían en nosotros.
          </p>
          <button
            onClick={() => navigate('/login')}
            className="bg-white text-secondary hover:bg-gray-100 px-12 py-5 rounded-full font-bold text-lg transition-all duration-300 hover:shadow-2xl hover:scale-105 inline-flex items-center gap-3"
          >
            <span>Acceder al Sistema</span>
            <PawPrint size={24} />
          </button>
        </div>
      </section>

      {/* Contact Section */}
      <section id="contacto" className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <span className="text-primary font-semibold text-sm uppercase tracking-wider">Contacto</span>
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-bold text-secondary mt-3 mb-4">
              ¿Cómo podemos ayudarte?
            </h2>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                icon: Phone,
                title: 'Teléfono',
                info: '+57 300 123 4567',
                subinfo: 'Lun - Sáb: 8:00 AM - 8:00 PM',
                color: 'bg-green-500'
              },
              {
                icon: Mail,
                title: 'Email',
                info: 'contacto@veterinariahumboldt.com',
                subinfo: 'Respuesta en menos de 24 horas',
                color: 'bg-blue-500'
              },
              {
                icon: MapPin,
                title: 'Ubicación',
                info: 'Calle 100 #15-20, Bogotá',
                subinfo: 'Zona Norte - Fácil acceso',
                color: 'bg-red-500'
              },
            ].map((contact, index) => (
              <div 
                key={index}
                className="text-center p-8 rounded-3xl border border-gray-100 hover:shadow-xl transition-shadow duration-300"
              >
                <div className={`${contact.color} w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-6`}>
                  <contact.icon className="text-white" size={28} />
                </div>
                <h3 className="text-xl font-bold text-secondary mb-2">{contact.title}</h3>
                <p className="text-gray-800 font-medium">{contact.info}</p>
                <p className="text-gray-500 text-sm mt-1">{contact.subinfo}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-secondary text-white py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-4 gap-12">
            <div className="md:col-span-2">
              <div className="flex items-center gap-3 mb-6">
                <img 
                  src="/LogoClinicaVeterinaria.png" 
                  alt="Logo" 
                  className="h-12 w-auto"
                />
                <span className="text-xl font-bold">Veterinaria Humboldt</span>
              </div>
              <p className="text-white/70 leading-relaxed max-w-md">
                Más de 10 años cuidando a las mascotas de Colombia con amor, 
                dedicación y profesionalismo.
              </p>
            </div>
            
            <div>
              <h4 className="font-bold mb-4">Enlaces Rápidos</h4>
              <ul className="space-y-2 text-white/70">
                <li><button onClick={() => scrollToSection('inicio')} className="hover:text-primary transition-colors">Inicio</button></li>
                <li><button onClick={() => scrollToSection('servicios')} className="hover:text-primary transition-colors">Servicios</button></li>
                <li><button onClick={() => scrollToSection('nosotros')} className="hover:text-primary transition-colors">Nosotros</button></li>
                <li><button onClick={() => scrollToSection('contacto')} className="hover:text-primary transition-colors">Contacto</button></li>
              </ul>
            </div>
            
            <div>
              <h4 className="font-bold mb-4">Horarios</h4>
              <ul className="space-y-2 text-white/70">
                <li>Lunes - Viernes: 8AM - 8PM</li>
                <li>Sábados: 8AM - 6PM</li>
                <li>Domingos: 9AM - 2PM</li>
                <li className="text-primary font-semibold">Emergencias: 24/7</li>
              </ul>
            </div>
          </div>
          
          <div className="border-t border-white/10 mt-12 pt-8 text-center text-white/50">
            <p>© 2024 Clínica Veterinaria Humboldt. Todos los derechos reservados.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
